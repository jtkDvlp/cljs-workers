(ns cljs-workers.test
  (:require [cljs.core.async :refer [<!]]
            [cljs-workers.core :as main]
            [cljs-workers.worker :as worker])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn app
  []
  (let [worker-pool
        (main/create-pool 2 "worker.js")

        print-result
        (fn [result-chan]
          (go
            (let [result (<! result-chan)]
              (.debug js/console
                      (str (:state result))
                      result))))]

    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10}}))
    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:d]}))
    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:c]}))
    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:c]} :transfer [:d]}))))

(defn worker
  []
  (worker/register
   :mirror
   (fn [arguments]
     arguments))

  (worker/bootstrap))

(if (and (main/supported?) (main/main?))
  (app)
  (worker))
