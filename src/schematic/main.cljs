(ns ^:figwheel-hooks schematic.main
  (:require
    [goog.dom :as dom]
    [goog.functions :as fun]
    [schematic.constant :as c]
    [schematic.block :as block]))

(def workspace)

;; Things we need to do before Figwheel reloads the code
(defn ^:before-load teardown []
  (when workspace
        (.dispose workspace)
        (dom/removeChildren (dom/getElement "ide"))))

(.defineBlocksWithJsonArray js/Blockly
  (clj->js (mapv block/get-def [block/schema
                                block/string-base
                                block/string-regex
                                block/string-format
                                block/number
                                block/object-base
                                block/object-property])))

;; Blockly generator for JSON Schema.
;; Attach the instance to the global Blockly object (in the JS namespace).
;; Also attach code-generating functions for each block.
(def generator
  (let [inst (js/Blockly.Generator. "JsonSchema")]
    (set! (.-JsonSchema js/Blockly) inst)
    (set! (.-schema inst) (block/get-generator block/schema))
    (set! (.-string_base inst) (block/get-generator block/string-base))
    (set! (.-string_regex inst) (block/get-generator block/string-regex))
    (set! (.-string_format inst) (block/get-generator block/string-format))
    (set! (.-number inst) (block/get-generator block/number))
    (set! (.-object_base inst) (block/get-generator block/object-base))
    (set! (.-object_property inst) (block/get-generator block/object-property))
    inst))

(defn toolbox [name clr blocks]
  {:kind :category
   :colour clr
   :name name
   :contents (mapv #(-> {:kind :block :type (block/get-type %)}) blocks)})

(set! workspace
  (.inject js/Blockly "ide"
    (clj->js {:toolbox
               {:kind :categoryToolbox
                :contents [(toolbox "Schema" c/colour-schema [block/schema])
                           (toolbox "String" c/colour-string [block/string-base
                                                              block/string-regex
                                                              block/string-format])
                           (toolbox "Number" c/colour-number [block/number])
                           (toolbox "Object" c/colour-object [block/object-base
                                                              block/object-property])]}})))

(defonce schema-viewer
  (.edit js/ace (dom/getElement "schema-viewer")
                #js {:mode "ace/mode/json"
                     :readOnly true}))

(defn display-schema []
  (let [display (.getSession schema-viewer)
        blocks (filter #(= "schema" (.-type %)) (.getTopBlocks workspace))]
    (when-let [block (first blocks)]
      (.setValue display (-> (.blockToCode generator block)
                             (js/JSON.parse)
                             (js/JSON.stringify nil 2))))))

(.addChangeListener workspace
  (fun/debounce display-schema 500))
