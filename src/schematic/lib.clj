(ns schematic.lib)

(defmacro defgen
  "Little sugar for code generator functions.
  Automatically transform Clojure data to JSON documents."
  [f] `(fn [block#]
         (-> (~f block#)
             (cljs.core/clj->js)
             (js/JSON.stringify))))
