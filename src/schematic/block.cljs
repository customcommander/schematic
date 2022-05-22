(ns schematic.block
  (:require [schematic.constant :as c])
  (:require-macros [schematic.lib :as lib]))

(defn get-def [blk]
  (:def blk))

(defn get-type [blk]
  (-> blk :def :type))

(defn get-generator [blk]
  (:gen blk))

(defn- statement->code
  [bl name default]
  (let [code (.statementToCode js/Blockly.JsonSchema bl name)]
    (if (not (empty? code))
        (js/JSON.parse code)
        default)))

(def schema
  {:def {:type :schema
         :colour c/colour-schema
         :message0 "schema %1 %2"
         :args0 [{:type :input_dummy}
                 {:type :input_statement
                  :name "SCHEMA"
                  :check :schema_type}]}
   :gen (lib/defgen (fn [bl]
                      (statement->code bl "SCHEMA" {})))})

(def string-base
  {:def {:type :string_base
         :colour c/colour-string
         :message0 "any string"
         :previousStatement :schema_type}
   :gen (lib/defgen (fn []
                      {:type :string}))})

(def string-regex
  {:def {:type :string_regex
         :colour c/colour-string
         :message0 "regex: /%1/"
         :args0 [{:type :field_input
                  :name "REGEX"
                  :spellcheck false}]
         :previousStatement :schema_type}
   :gen (lib/defgen (fn [bl]
                      (let [f (.getFieldValue bl "REGEX")]
                        (if (not (empty? f))
                            {:type :string :pattern f}
                            {:type :string}))))})

(def string-format
  {:def {:type :string_format
         :colour c/colour-string
         :previousStatement :schema_type
         :message0 "format: %1"
         :args0 [{:type :field_dropdown
                  :name "STRING_FORMAT"
                  :options [["date"                  "date"                 ]
                            ["date-time"             "date-time"            ]
                            ["duration"              "duration"             ]
                            ["email"                 "email"                ]
                            ["hostname"              "hostname"             ]
                            ["idn-email"             "idn-email"            ]
                            ["idn-hostname"          "idn-hostname"         ]
                            ["ipv4"                  "ipv4"                 ]
                            ["ipv6"                  "ipv6"                 ]
                            ["iri"                   "iri"                  ]
                            ["iri-reference"         "iri-reference"        ]
                            ["json-pointer"          "json-pointer"         ]
                            ["regex"                 "regex"                ]
                            ["relative-json-pointer" "relative-json-pointer"]
                            ["time"                  "time"                 ]
                            ["uri"                   "uri"                  ]
                            ["uri-reference"         "uri-reference"        ]
                            ["uri-template"          "uri-template"         ]
                            ["uuid"                  "uuid"                 ]]}]}
   :gen (lib/defgen (fn [bl]
                      {:type :string
                       :format (.getFieldValue bl "STRING_FORMAT")}))})

(def number
  {:def {:type :number
         :message0 "any number"
         :previousStatement :schema_type}
   :gen (lib/defgen (fn []
                      {:type :number}))})

(defn- is-checked? [bl field-name]
  "Assume field 'field-name' is a checkbox.
  Return whether checkbox is checked or not."
  (= "TRUE" (.getFieldValue bl field-name)))

(defn- property-name [bl]
  (.getFieldValue bl "property_name"))

(defn- get-properties [bl]
  (loop [prop (.getInputTargetBlock bl "object_props")
         props []]
    (if prop
        (recur (.getNextBlock prop)
               (conj props {:name (.getFieldValue prop "property_name")
                            :required (is-checked? prop "property_required")
                            :code (.statementToCode js/Blockly.JsonSchema prop "SCHEMA")}))
        props)))

(defn- object-base-generator [bl]
  (let [properties (remove #(or (empty? (:name %)) (empty? (:code %))) (get-properties bl))
        properties-required (filter #(:required %) properties)]
    (merge {:type :object}
           {:additionalProperties (is-checked? bl "object_additional_props")}
           (when (not (empty? properties-required))
             {:required (map :name properties-required)})
           (when (not (empty? properties))
             {:properties (into {} (mapv #(vector (:name %)
                                                  (js/JSON.parse (:code %)))
                                         properties))}))))

(def object-base
  {:def {:type :object_base
         :colour c/colour-object
         :previousStatement nil
         :message0 "object %1 %2 %3 allow other properties"
         :args0 [{:type :input_dummy}
                 {:type :input_statement
                  :name :object_props
                  :check [:property]}
                 {:type :field_checkbox
                  :name :object_additional_props
                  :checked true}]}
   :gen (lib/defgen object-base-generator)})

(def object-property
  {:def {:type :object_property
         :colour c/colour-object
         :previousStatement :property
         :nextStatement :property
         :message0 "property %1 is %2 required %3 %4"
         :args0 [{:type :field_input
                  :name :property_name}
                 {:type :field_checkbox
                  :name :property_required
                  :checked false}
                 {:type :input_dummy}
                 {:type :input_statement
                  :name "SCHEMA"
                  :check [:schema_type]}]}
   :gen (lib/defgen (fn [bl]
                      nil))})
