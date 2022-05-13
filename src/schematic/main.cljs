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

(.defineBlocksWithJsonArray js/Blockly
  (clj->js (mapv block/get-def [block/schema
                                block/stringg])))

;; Blockly generator for JSON Schema.
;; Attach the instance to the global Blockly object (in the JS namespace).
;; Also attach code-generating functions for each block.
(def generator
  (let [inst (js/Blockly.Generator. "JsonSchema")]
    (set! (.-JsonSchema js/Blockly) inst)
    (set! (.-schema inst) (block/get-generator block/schema))
    (set! (.-string inst) (block/get-generator block/stringg))
    inst))

(defn toolbox [name blocks]
  {:kind :category
   :name name
   :contents (mapv #(-> {:kind :block :type (block/get-type %)}) blocks)})

(set! workspace
  (.inject js/Blockly "ide"
    (clj->js {:toolbox
               {:kind :categoryToolbox
                :contents [(toolbox "Schema" [block/schema])
                           (toolbox "String" [block/stringg])]}})))

(defonce schema-viewer
  (.edit js/ace (dom/getElement "schema-viewer")
                #js {:mode "ace/mode/json"
                     :readOnly true}))

(defn display-schema []
  (let [display (.getSession schema-viewer)
        block (first (.getTopBlocks workspace))]
    (.setValue display (-> (.blockToCode generator block)
                           (js/JSON.parse)
                           (js/JSON.stringify nil 2)))))

(.addChangeListener workspace
  (fun/debounce display-schema 500))
