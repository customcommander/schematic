(ns schematic.block)

(def schema
  {:type :schema
   :message0 "schema %1 %2"
   :args0 [{:type :input_dummy}
           {:type :input_statement
            :name "SCHEMA"}]})

(defn schema->code []
  "{}")
