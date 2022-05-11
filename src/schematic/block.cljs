(ns schematic.block
  (:require-macros [schematic.lib :as lib :refer [defgen]]))

(defn get-def [blk]
  (:def blk))

(defn get-type [blk]
  (-> blk :def :type))

(defn get-generator [blk]
  (:gen blk))

(def schema
  {:def {:type :schema
         :message0 "schema %1 %2"
         :args0 [{:type :input_dummy}
                 {:type :input_statement
                  :name "SCHEMA"}]}
   :gen (defgen (fn [] {:x 42}))})

(def stringg
  {:def {:type :string
         :message0 "any string"}
   :gen (defgen (fn [] {:type :string}))})
