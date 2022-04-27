(ns ^:figwheel-hooks core.main
  (:require
    [goog.dom :as dom]
    [goog.functions :as fun]
    [core.block :as block]))

(def workspace)

;; Things we need to do before Figwheel reloads the code
(defn ^:before-load teardown []
  (when workspace
        (.dispose workspace)
        (dom/removeChildren (dom/getElement "blocklyDiv"))))

(.defineBlocksWithJsonArray js/Blockly
  (clj->js [block/schema]))

(def generator
  (js/Blockly.Generator. "JsonSchemaGenerator"))

(set! (.-schema generator) block/schema->code)

(set! workspace
  (.inject js/Blockly "blocklyDiv"
    (clj->js {:toolbox
               {:kind :flyoutToolbox
                :contents [{:kind :block :type (:type block/schema)}]}})))

(.addChangeListener workspace
  (fun/debounce (fn []
                  (js/console.log (.workspaceToCode generator workspace)))
                500))
