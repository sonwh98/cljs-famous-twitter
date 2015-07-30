(ns ^:figwheel-always twitter.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [com.kaicode.infamous :as infamous :refer [events->chan famous]]
            [cljs.core.async :refer [alts!]]))

(enable-console-print!)

(defonce Size (.. famous -components -Size))
(defonce ABSOLUTE (.. Size -ABSOLUTE))
(defonce DEFAULT (.. Size -RELATIVE))

(def sections [{:name "Home" :tweet-number 6}

               ])

(def scene-graph {:node/id       "twitterus"
                  :node/children [{:node/id            "header"
                                   :node/size-mode     [DEFAULT ABSOLUTE]
                                   :node/absolute-size [nil 100]
                                   :node/components    [{:component/type :DOMElement
                                                         :fontSize       "30px"
                                                         :lineHeight     "100px"
                                                         :classes        ["header"]
                                                         :content        "Login"}]}
                                  {:node/id                "swapper"
                                   :node/differential-size [nil -200 nil]
                                   :node/position          [0 100]
                                   :node/components        [{:component/type :DOMElement}]
                                   :node/children          [{:node/id         "section-Home"
                                                             :node/align      [0 0 0]
                                                             :node/components [{:component/type :DOMElement
                                                                                :overflow-y     "scroll"
                                                                                :overflow-x     "hidden"}
                                                                               {:component/type :Align}]
                                                             :node/children   [{:node/size-mode     [DEFAULT ABSOLUTE]
                                                                                :node/absolute-size [nil 100]
                                                                                :node/position      [0 100]
                                                                                :node/components    [{:component/type  :DOMElement
                                                                                                      :backgroundColor (str "#" (.toString (rand-int 16rFFFFFF) 16))
                                                                                                      :boxingSize      "border-box"
                                                                                                      :lineHeight      "100px"
                                                                                                      :borderBottom    "1px solid black"
                                                                                                      :font-size       "12px"
                                                                                                      :content         (str "Login" )}]
                                                                                }]}]}
                                  ]})



(defn get-dom-element-and-align-component [section-name]
  (let [footer-section-node (infamous/get-node-by-id (str "footer-" section-name))
        section-node (infamous/get-node-by-id (str "section-" section-name))
        dom-element (infamous/get-famous-component-by-type-name footer-section-node "DOMElement")
        align-component (infamous/get-famous-component-by-type-name section-node "Align")]
    [dom-element align-component]))

(defonce TRANSITION-DURATION 500)

(defn show [section-name]
  (let [[section-dom-element section-align] (get-dom-element-and-align-component section-name)
        header-node (infamous/get-node-by-id "header")
        header-dom-element (infamous/get-famous-component-by-type-name header-node "DOMElement")]
    (.. header-dom-element (setContent section-name))
    (.. section-dom-element (removeClass "off") (addClass "on"))
    (.. section-align (set 0 0 0 (clj->js {:duration TRANSITION-DURATION})))))

(defn hide [section-name]
  (let [[dom-element align-component] (get-dom-element-and-align-component section-name)]
    (.. dom-element (removeClass "on") (addClass "off"))
    (.. align-component (set 1 0 0 (clj->js {:duration TRANSITION-DURATION})))))

(defn hide-all-sections-except [section-name]
  (let [selected-footer-button-node (infamous/get-node-by-id (str "footer-" section-name))
        footer-button-nodes (:node/children (infamous/get-node-by-id "footer"))
        unselected-nodes (filter #(not= % selected-footer-button-node) footer-button-nodes)
        unselected-section-names (map #(:twitter/section-name %) unselected-nodes)]
    (doseq [section-name unselected-section-names]
      (hide section-name))))

(defn switch-on [section-name]
  (show section-name)
  (hide-all-sections-except section-name))

(infamous/render-scene-graph scene-graph "body")            ;render the scene-graph and mount it on the "body" css selector

