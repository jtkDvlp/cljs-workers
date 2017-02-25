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

          result
          (handler arguments)

          message
          (-> result
              (merge {:state :success})
              clj->js)

          transfer
          (->> (:transfer result)
               (map keyword)
               (select-keys result)
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

  (let [data
        (.-data event)

        handler
        (keyword (.-handler data))

        arguments
        (js->clj (.-arguments data) :keywordize-keys true)]

    (do-respond! handler arguments)))

(defn bootstrap
  []
  (aset js/self "onmessage" handle-request!))
