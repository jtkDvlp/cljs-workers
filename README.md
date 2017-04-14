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

...will follow...

#### Quick guide

Workers have their own context, not the global window context. So there is no document / DOM and some other things are also not present.

To handle data between these two contexts you have to copy or [transfer](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Using_web_workers#Transferring_data_to_and_from_workers_further_details) your objects. Consider, you can copy simple structured objects and transfer [transferables](https://developer.mozilla.org/en-US/docs/Web/API/Transferable), everything else will cause an error. But don´t be worried, most the time that´s no problem.

Since there are two contexts, you have to provide two script threads / procedures. You can handle this by providing two script files or you can also provide one file handling both. When using the second way pay attention on what you get with the current context (workers have no DOM).

For full documentation see the [web worker api](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Using_web_workers).

## Appendix

I´d be thankful to receive patches, comments and constructive criticism.

Hope the package is useful :-)
