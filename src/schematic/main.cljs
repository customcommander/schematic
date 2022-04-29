(ns ^:figwheel-hooks schematic.main
  (:require
    [goog.dom :as dom]
    [goog.functions :as fun]
    [schematic.block :as block]))

(def workspace)

;; Things we need to do before Figwheel reloads the code
(defn ^:before-load teardown []
  (when workspace
        (.dispose workspace)
        (dom/removeChildren (dom/getElement "ide"))))

;; Register all custom blocks
(.defineBlocksWithJsonArray js/Blockly
  (clj->js [block/schema]))

(def generator
  (js/Blockly.Generator. "JsonSchemaGenerator"))

(set! (.-schema generator) block/schema->code)

(set! workspace
  (.inject js/Blockly "ide"
    (clj->js {:toolbox
               {:kind :categoryToolbox
                :contents [{:kind :category
                            :name "Schema"
                            :contents [{:kind :block
                                        :type (:type block/schema)}]}]}})))

(defonce schema-viewer
  (.edit js/ace (dom/getElement "schema-viewer")
                #js {:mode "ace/mode/json"
                     :readOnly true}))

(defn display-schema []
  (let [display (.getSession schema-viewer)
        block (first (.getTopBlocks workspace))]
    (.setValue display (.blockToCode generator block))))

(.addChangeListener workspace
  (fun/debounce display-schema 500))
