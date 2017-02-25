(ns cljs-workers.test
  (:require [cljs-workers.core :as main]
            [cljs-workers.worker :as worker]))

(defn app
  []
  (let [worker-pool (main/create-pool 2 "js/worker/worker.js")]
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10}} #(.debug js/console %))
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:d]} #(.debug js/console %))
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:c]} #(.debug js/console %))
    (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:c]} :transfer [:d]} #(.debug js/console %))))

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
