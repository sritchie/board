(ns board.core
  (:require [goog.events :as events]))

(def socket (new js/window.WebSocket "ws://localhost:8080"))

(set! (.-onmessage socket) #(-> % msg->coord draw-circ))

(defn $ [selector]
  (.querySelector js/document (name selector)))

(def board ($ :#board))

(defn circ [ctx x y d]
  (doto ctx
    .beginPath
    (.arc x y d 0 (* 2 Math/PI))
    .closePath
    .fill))

(defn draw-circ [[x y]]
  (-> board (.getContext "2d") (circ x y 5)))

(def buttons
  {goog.events.BrowserEvent.MouseButton/LEFT   :left
   goog.events.BrowserEvent.MouseButton/MIDDLE :middle
   goog.events.BrowserEvent.MouseButton/RIGHT  :right})

(def pressed-buttons (atom #{}))

(defn pressed? [button]
  (@pressed-buttons button))

(defn mouse-move-callback [event]
  (let [x (.-clientX event)
        y (.-clientY event)]
    (when (pressed? :left)
      (.send socket (str x "," y))
      (draw-circ [x y]))))

(defn button-event-callback [f event]
  (->> event .-button buttons (swap! pressed-buttons f)))

(defn msg->coord [message]
  (let [[x y] (.split (.-data message) ",")]
    [(js/parseInt x) (js/parseInt y)]))

(defn listen [element event callback]
  (events/listen element (name event) callback))

(listen board (name :mousemove) mouse-move-callback)

(doto js/window
  (listen :mousedown (partial button-event-callback conj))
  (listen :mouseup   (partial button-event-callback disj)))
