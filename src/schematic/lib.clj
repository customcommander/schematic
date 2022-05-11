(ns schematic.lib)

(defmacro defgen
  "Blockly generator. Takes a block and returns a string.
  Must return normal Clojure data. It will be converted to
  JavaScript and serialised automatically."
  [f] `(fn [block#]
         (-> (~f block#)
             (cljs.core/clj->js)
             (js/JSON.stringify))))
