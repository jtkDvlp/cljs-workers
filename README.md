[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/jtkDvlp/cljs-workers/blob/master/LICENSE)
[![Clojars Project](https://img.shields.io/clojars/v/cljs-workers.svg)](https://clojars.org/cljs-workers)

# Web workers for clojurescript

This [clojurescript](https://clojurescript.org/) library wraps the [web worker api](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Using_web_workers) and provides an simple way for multithreading within browsers with cljs.

## Getting started

### Get it / add dependency

Add the following dependency to your `project.cljs`:<br>
[![Clojars Project](https://img.shields.io/clojars/v/cljs-workers.svg)](https://clojars.org/cljs-workers)

### Usage

To understand web workers itself see the [web worker api](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Using_web_workers). Or see the [quick guide](#quick-guide).

The following example handling both the browser and the worker within one script. The script provides an worker mirroring its inputs as outputs and testing four worker calls. Two of them will success and two of them will fail.

```clojure
(ns cljs-workers.test
  (:require [cljs.core.async :refer [<!]]
            [cljs-workers.core :as main]
            [cljs-workers.worker :as worker])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Setup the browser path (handling both in one file)
(defn app
  []
  (let [;; you can create one worker or a pool (async channel of workers)
        worker-pool
        (main/create-pool 2 "js/worker/worker.js")

        ;; a "do-with-pool" or "-worker" (see below) will return immediately and give you a result channel. So to print the result you have to handle the channel
        print-result
        (fn [result-chan]
          (go
            (let [result (<! result-chan)]
              (.debug js/console
                      (str (:state result))
                      result))))]

    ;; Copy all simple values
    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10}}))
    ;; Copy the simple values and transfer the ArrayBuffer
    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:d]}))
    ;; Copy the simple values and transfer the ArrayBuffer, but transfer (browser thread) will fail cause the wrong value and the wrong type is marked to do so
    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:d]} :transfer [:c]}))
    ;; Copy the simple values and transfer the ArrayBuffer, but transfer mirroring (worker thread) will fail cause the wrong value and the wrong type is marked to do so
    (print-result (main/do-with-pool! worker-pool {:handler :mirror, :arguments {:a "Hallo" :b "Welt" :c 10 :d (js/ArrayBuffer. 10) :transfer [:c]} :transfer [:d]}))))

;; Setup the worker path (handling both in one file)
(defn worker
  []
  (worker/register
   :mirror
   (fn [arguments]
     arguments))

  (worker/bootstrap))

;; Decide which path to setup
(if (main/main?)
  (app)
  (worker))
```

#### Quick guide

Workers have their own context, not the global window context. So there is no document / DOM and some other things are also not present.

[To handle data](https://developer.mozilla.org/de/docs/Web/API/Worker/postMessage) between these two contexts you have to copy or [transfer](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Using_web_workers#Transferring_data_to_and_from_workers_further_details) your values / objects. Consider, you can copy values / objects handled by the [structured clone algorithm](https://developer.mozilla.org/de/docs/Web/API/Web_Workers_API/Structured_clone_algorithm) and transfer [transferables](https://developer.mozilla.org/en-US/docs/Web/API/Transferable), everything else will cause an error. But don´t be worried, most the time that´s no problem.

Since there are two contexts, you have to provide two script threads / procedures. You can handle this by providing two script files or you can also provide one file handling both. When using the second way pay attention on what you get with the current context (workers have no DOM).

For full documentation see the [web worker api](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Using_web_workers).

## Appendix

I´d be thankful to receive patches, comments and constructive criticism.

Hope the package is useful :-)
