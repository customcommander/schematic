(ns schematic.block
  (:require-macros [schematic.lib :as lib]))

(defn get-def [blk]
  (:def blk))

(defn get-type [blk]
  (-> blk :def :type))

(defn get-generator [blk]
  (:gen blk))

(defn statement->code
  [bl name default]
  (let [code (.statementToCode js/Blockly.JsonSchema bl name)]
    (if (not (empty? code))
        (js/JSON.parse code)
        default)))

(def schema
  {:def {:type :schema
         :colour 264
         :message0 "schema %1 %2"
         :args0 [{:type :input_dummy}
                 {:type :input_statement
                  :name "SCHEMA"}]}
   :gen (lib/defgen (fn [bl]
                      (statement->code bl "SCHEMA" {})))})

(def stringg
  {:def {:type :string
         :colour 118
         :message0 "any string"
         :previousStatement nil}
   :gen (lib/defgen (fn []
                      {:type :string}))})

(def string-regex
  {:def {:type :string_regex
         :colour 118
         :message0 "regex: /%1/"
         :args0 [{:type :field_input
                  :name "REGEX"
                  :spellcheck false}]
         :previousStatement nil}
   :gen (lib/defgen (fn [bl]
                      (let [f (.getFieldValue bl "REGEX")]
                        (if (not (empty? f))
                            {:type :string :pattern f}
                            {:type :string}))))})

(def string-format
  {:def {:type :string_format
         :colour 118
         :previousStatement nil
         :message0 "format: %1"
         :args0 [{:type :field_dropdown
                  :name "STRING_FORMAT"
                  :options [["date" "date"]
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
                            ["uuid" "uuid"                                  ]]}]}
   :gen (lib/defgen (fn [bl]
                      {:type :string
                       :format (.getFieldValue bl "STRING_FORMAT")}))})

(def number
  {:def {:type :number
         :colour 208
         :message0 "any number"
         :previousStatement nil}
   :gen (lib/defgen (fn []
                      {:type :number}))})
