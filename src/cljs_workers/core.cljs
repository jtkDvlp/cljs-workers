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
   (pool 5))

  ([count]
   (pool count "js/compiled/workers.js"))

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

