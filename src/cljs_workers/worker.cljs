(ns cljs-workers.worker)

(def handlers
  (atom {}))

(defn register
  [id fun]
  (swap! handlers assoc id fun))

(defn- do-respond!
  [handler arguments]
  (try
    (let [handler
          (@handlers handler)

          message
          (-> (handler arguments)
              (merge {:state :success}))

          transfer
          (->> (:transfer message)
               (map keyword))]

      (if (seq transfer)
        (.postMessage js/self (clj->js message) (clj->js (vals (select-keys message transfer))))
        (.postMessage js/self (clj->js message))))
    (catch js/Object e
      (->> {:state :error, :message (.toString e)}
           clj->js
           (.postMessage js/self)))))

(defn- handle-request!
  [event]
  (let [data
        (.-data event)

        handler
        (keyword (.-handler data))

        arguments
        (js->clj (.-arguments data) :keywordize-keys true)]

    (do-respond! handler arguments)))

(aset js/self "onmessage" handle-request!)
