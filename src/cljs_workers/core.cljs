(ns cljs-workers.core
  (:require [cljs.core.async :refer [chan promise-chan <! >! put!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn supported?
  []
  (-> js/self
      .-Worker
      undefined?
      not))

(defn worker?
  []
  (-> js/self
      .-document
      undefined?))

(def main?
  (complement worker?))

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
     (dotimes [_ count]
       (put! workers (create-one script)))
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
  [worker {:keys [handler arguments transfer] :as request}]
  (let [result
        (promise-chan)

        put-result!
        (partial put! result)]

    (->> (comp put-result! handle-response!)
         (aset worker "onmessage"))

    (try
      (do-request! worker request)
      (catch js/Object e
        (put! result {:state :error, :error e})))

    result))

(defn do-with-pool!
  [pool {:keys [handler arguments transfer] :as request}]
  (let [result* (promise-chan)]
    (go
      (let [worker
            (<! pool)

            result-chan
            (do-with-worker! worker request)

            result
            (<! result-chan)]

        (>! pool worker)
        (>! result* result)))

    result*))
