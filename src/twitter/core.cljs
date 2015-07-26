(ns ^:figwheel-always twitter.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [com.famous.Famous]
            [reagent.core :as reagent]
            [twitter.util :as util :refer [events->chan get-node-by-id render-scene-graph]]
            [cljs.core.async :refer [alts!]]))

(enable-console-print!)

(defonce famous js/famous)
(defonce Size (.. famous -components -Size))

(defonce ABSOLUTE (.. Size -ABSOLUTE))
(defonce DEFAULT (.. Size -RELATIVE))

(def sections [{:id "Home" :tweet-number 50}
               {:id "Discover" :tweet-number 50}
               {:id "Connect" :tweet-number 50}
               {:id "Me" :tweet-number 25}])

(def scene-graph {:node/id       "twitterus"
                  :node/children [{:node/id            "header"
                                   :node/size-mode     [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/components    [{:component/type :DOMElement
                                                         :fontSize       "30px"
                                                         :lineHeight     "100px"
                                                         :classes        ["header"]
                                                         :content        "Twitter"}]}

                                  {:node/id                "swapper"
                                   :node/differential-size [nil -200 nil]
                                   :node/position          [0 100]
                                   :node/components        [{:component/type :DOMElement}]
                                   :node/children          (for [{:keys [id]} sections]
                                                             {:node/id         (str "section-" id)
                                                              :node/components [{:component/type :DOMElement
                                                                                 :overflow-y     "scroll"
                                                                                 :overflow-x     "hidden"}
                                                                                {:component/type :Align}]
                                                              :node/children   (for [i (range 12)]
                                                                                 {:node/size-mode     [DEFAULT ABSOLUTE]
                                                                                  :node/absolute-size [nil 100]
                                                                                  :node/position      [0 (* 100 i)]
                                                                                  :node/components    [{:component/type  :DOMElement
                                                                                                        :backgroundColor (str "#" (.toString (rand-int 16rFFFFFF) 16))
                                                                                                        :boxingSize      "border-box"
                                                                                                        :lineHeight      "100px"
                                                                                                        :borderBottom    "1px solid black"
                                                                                                        :font-size       "12px"
                                                                                                        :content         (str "tweet" i)}]
                                                                                  })})}
                                  {:node/id            "footer"
                                   :node/size-mode     [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/align         [0 1]
                                   :node/mount-point   [0 1]
                                   :node/components    [{:component/type :DOMElement
                                                         }]
                                   :node/children      (let [num-sections (count sections)]
                                                         (for [i (range num-sections)
                                                               :let [{:keys [id tweet-number]} (sections i)]]
                                                           {:node/id                id
                                                            :node/align             [(/ i num-sections)]
                                                            :node/proportional-size [(/ 1 num-sections)]
                                                            :node/components        [{:component/type :DOMElement
                                                                                      :textAlign      "center"
                                                                                      :lineHeight     "100px"
                                                                                      :fontSize       "18px"
                                                                                      :cursor         "pointer"
                                                                                      :classes        (if (= i 0)
                                                                                                        ["navigation" "on"]
                                                                                                        ["navigation" "off"])
                                                                                      :content        id}]}))}]})

(util/save scene-graph)

(render-scene-graph "twitterus")

(def channels (for [section-button-node (:node/children (get-node-by-id "footer"))]
                (events->chan section-button-node "tap" (fn [] section-button-node))))

(defn switch-on [component]
  (.. component (removeClass "off") (addClass "on")))

(defn switch-off [component]
  (.. component (removeClass "on") (addClass "off")))

(defn get-famous-component [component-name]

  )

(go
  (while true
    (let [[selected-button-node channel] (alts! channels)
          section-button-nodes (:node/children (get-node-by-id "footer"))
          id (:node/id selected-button-node)
          selected-section-node (-> (get-node-by-id (str "section-" id)) :node/famous-node)
          off-section-nodes (filter #(not= % selected-button-node) section-button-nodes)
          famous-node (:node/famous-node selected-button-node)
          components (.. famous-node getComponents)]
      
      (doseq [c (.. selected-section-node getComponents)
              :let [component-name (.. c -constructor -name)]]
        (if (= component-name "Align")
          (.. c (set 1 0 0 (clj->js {:duration 500})))))

      (doseq [c components
              :let [component-name (.. c -constructor -name)]]
        (if (= "DOMElement" component-name)
          (switch-on c)))

      (doseq [{famous-node :node/famous-node} off-section-nodes
              :let [align (.. famous-node getAlign)]]
        (doseq [c (.. famous-node getComponents)]
          (if (= "DOMElement" (.. c -constructor -name))
            (switch-off c)))))))
