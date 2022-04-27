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
        (dom/removeChildren (dom/getElement "ide"))))

(.defineBlocksWithJsonArray js/Blockly
  (clj->js [block/schema]))

(def generator
  (js/Blockly.Generator. "JsonSchemaGenerator"))

(set! (.-schema generator) block/schema->code)

(set! workspace
  (.inject js/Blockly "ide"
    (clj->js {:toolbox
               {:kind :flyoutToolbox
                :contents [{:kind :block :type (:type block/schema)}]}})))

(defonce schema-viewer
  (.edit js/ace (dom/getElement "schema-viewer")
                #js {:mode "ace/mode/json"
                     :readOnly true}))

(.addChangeListener workspace
  (fun/debounce (fn []
                  (let [code-str (.workspaceToCode generator workspace)
                        schema-viewer-session (.getSession schema-viewer)]
                    (.setValue schema-viewer-session (-> code-str
                                                         (js/JSON.parse)
                                                         (js/JSON.stringify nil 2)))))
                500))
