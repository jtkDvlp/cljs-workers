(ns cljs-workers.worker
  (:require [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def handlers
  (atom {}))

(defn register
  [id fun]
  (swap! handlers assoc id fun))

(defn- chan?
  [x]
  (satisfies? cljs.core.async.impl.protocols/ReadPort x))

(defn- do-respond!
  [data]
  (try
    (let [message
          (-> data
              (merge {:state :success})
              clj->js)

          transfer
          (->> (:transfer data)
               (map keyword)
               (select-keys data)
               vals)]

      (if (seq transfer)
        (.postMessage js/self message (clj->js transfer))
        (.postMessage js/self message)))

    (catch js/Object e
      (->> {:state :error, :message (.toString e)}
           clj->js
           (.postMessage js/self)))))

(defn- handle-request!
  [event]
  (try
    (let [data
          (.-data event)

          handler
          (@handlers (keyword (.-handler data)))

          arguments
          (js->clj (.-arguments data) :keywordize-keys true)

          result
          (handler arguments)]

      (if (chan? result)
        (go (do-respond! (<! result)))
        (do-respond! result)))

    (catch js/Object e
      (->> {:state :error, :message (.toString e)}
           clj->js
           (.postMessage js/self)))))

(defn bootstrap
  []
  (aset js/self "onmessage" handle-request!))
