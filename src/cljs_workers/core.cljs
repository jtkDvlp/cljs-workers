(ns cljs-workers.core
  (:require [cljs.core.async :refer [chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn supported?
  []
  (-> js/Worker
      undefined?
      not))

(defn create-one
  [script]
  (js/Worker. script))

(defn create-pool
  ([]
   (create-pool 5))

  ([count]
   (create-pool count "js/compiled/workers.js"))

  ([count script]
   (let [workers (chan count)]
     (go
       (dotimes [_ count]
         (>! workers (create-one script))))
     workers)))

(defn- do-request!
  [worker {:keys [handler arguments transfer] :as request}]
  (let [message
        (-> {:handler handler, :arguments arguments}
            clj->js)

        transfer
        (->> transfer
             (select-keys arguments)
             vals)]

    (if (seq transfer)
      (.postMessage worker message (clj->js transfer))
      (.postMessage worker message))))

(defn- handle-response!
  [event]
  (-> (.-data event)
      (js->clj :keywordize-keys true)))

(defn do-with-worker!
  ([worker request]
   (do-with-worker! worker request nil))

  ([worker {:keys [handler arguments transfer] :as request} fun]
   (when fun
     (->> (comp fun handle-response!)
          (aset worker "onmessage")))
   (try
     (do-request! worker request)
     (catch js/Object e
       (when fun
         (fun {:state :error, :error e}))))))

(defn do-with-pool!
  ([pool request]
   (do-with-pool! pool request nil))

  ([pool {:keys [handler arguments transfer] :as request} fun]
   (go
     (let [worker
           (<! pool)

           fun
           (fn [response]
             (go (>! pool worker))
             (when fun (fun response)))]

       (do-with-worker! worker request fun)))))
