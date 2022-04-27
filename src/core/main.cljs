(ns ^:figwheel-hooks core.main
  (:require
    [goog.dom :as dom]))

(def workspace)

(def ws-options
  {:toolbox {:kind :flyoutToolbox
   :contents [{:kind :block :type :controls_if}
              {:kind :block :type :controls_repeat_ext}
              {:kind :block :type :logic_compare}
              {:kind :block :type :math_number}
              {:kind :block :type :math_arithmetic}
              {:kind :block :type :text}
              {:kind :block :type :text_print}]}})

;; Things we need to do before Figwheel reloads the code
(defn ^:before-load teardown []
  (when workspace
        (.dispose workspace)
        (dom/removeChildren (dom/getElement "blocklyDiv"))))

(set! workspace
  (.inject js/Blockly "blocklyDiv" (clj->js ws-options)))
