(ns cljs-workers.test
  (:require [cljs-workers.core :as main]
            [cljs-workers.worker :as worker]))

(defn app
  []
  (let [worker-pool (main/create-pool 2 "js/worker/worker.js")
        print-result #(.debug js/console (str (:state %)) %)]
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10}} print-result)
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:d]} print-result)
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:c]} print-result)
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:c]} :transfer [:d]} print-result)))

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
