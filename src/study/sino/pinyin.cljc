(ns study.sino.pinyin
  "Functions for converting between different representations of Hànyǔ Pīnyīn.

    - The neutral tone is represented as 0 or 5 (or left out) if using digits.
    - Like in many Pīnyīn input methods, a V is treated as a Ü.
    - The non-standard Pīnyīn M is supported (as found in e.g. cc-cedict).
    - The very common érhuà-style final R is supported as well."
  (:require [clojure.string :as str]
            [study.sino.pinyin.patterns :as patterns]
            [study.sino.pinyin.data :as data]))

(defn- parse-int
  "Parses a string `s` into an integer."
  [s]
  #?(:clj  (Integer/parseInt s)
     :cljs (js/parseInt s)))

(defn with-umlaut
  "Replace the common substitute letter V in `s` with the proper Pinyin Ü."
  [s]
  (-> s
      (str/replace \v \ü)
      (str/replace \V \Ü)))

(defn with-diacritic
  "Get the diacriticised `char` based on Pinyin `tone` (0 through 5)."
  [tone char]
  (nth (data/diacritics char) tone))

(defn diacritic-index
  "Get the index of `syllable` where the Pinyin diacritic should be.

  Derived from the guideline at: http://www.pinyin.info/rules/where.html"
  [syllable]
  (when (string? syllable)
    (let [s (re-find #"[^\d]+" (str/lower-case syllable))]
      (cond
        (empty? s) nil
        (str/includes? s "a") (str/index-of s "a")
        (str/includes? s "e") (str/index-of s "e")
        (str/includes? s "ou") (str/index-of s "o")
        :else (if-let [index (str/last-index-of s "n")]
                (- index 1)
                (- (count s) 1))))))

(defn- handle-m
  "Handle the rare, non-standard final m in `syllable`."
  [syllable]
  (let [tone (parse-int (str (last syllable)))
        skip (if (= \M (first syllable)) 6 0)]
    (nth data/m-diacritics (+ tone skip))))

(defn digit->diacritic
  "Convert a `syllable` with an affixed tone digit into one with a diacritic.
  If converting more than a single syllable, use digits->diacritics instead!

  Note: this function can also take Pinyin finals as input!"
  [syllable]
  (cond
    (or (empty? syllable) (nil? syllable)) syllable
    (re-matches #"[mM]\d" syllable) (handle-m syllable)
    :else (let [tone           (parse-int (str (last syllable)))
                s*             (subs syllable 0 (dec (count syllable)))
                char           (nth syllable (diacritic-index syllable))
                char+diacritic (with-diacritic tone char)]
            (str/replace s* char char+diacritic))))

(defn- last-final
  "Take a string `s` with a single affixed tone digit as input and returns the
  longest allowed Pinyin final + the digit. The Pinyin final that is returned
  is the one immediately before the digit, i.e. the last final.

  This is a helper function used by diacritic-string to find the bounds of the
  last Pinyin final."
  [s]
  (let [digit  (last s)
        end    (dec (count s))                              ; affixed digit
        length (if (< end 4) end 4)                         ; most will be <4
        start  (- end length)]
    (loop [candidate (subs s start end)]
      (cond
        (empty? candidate) nil
        (contains? data/finals (str/lower-case candidate)) (str candidate digit)
        :else (recur (apply str (rest candidate)))))))

(defn- handle-r
  "Handle the common, special-case final r in the string `s`."
  [s]
  (str/replace s #"\d" ""))

(defn- diacritic-string
  "Take a string with a single affixed tone digit as input and substitutes the
  digit with a tone diacritic. The diacritic is placed in the Pinyin final
  immediately before tone digit.

  This is a helper function used by digits->diacritics to convert tone digits
  into diacritics."
  [s]
  (if (contains? #{"r5" "R5" "r0" "R0"} (str/trim s))
    (handle-r s)
    (let [final           (last-final s)
          final+diacritic (digit->diacritic final)
          ;; prefix = preceding neutral tone syllables + the initial
          prefix          (subs s 0 (- (count s) (count final)))]
      (str prefix final+diacritic))))

(defn digits->diacritics
  "Convert a Pinyin string `s` with tone digits into one with tone diacritics."
  [s & {:keys [v-as-umlaut?] :or {v-as-umlaut? false}}]
  (if (string? s)
    (let [s*                (if v-as-umlaut? (with-umlaut s) s)
          digit-strings     (re-seq #"[^\d]+\d" s*)
          diacritic-strings (map diacritic-string digit-strings)
          suffix            (re-seq #"[^\d]+$" s*)]
      (apply str (concat diacritic-strings suffix)))
    s))

(defn no-diacritics
  "Remove Pinyin diacritics from the input string `s`."
  ([s] (no-diacritics s data/diacritic-patterns))
  ([s [[replacement match] & xs]]
   (if (nil? match)
     s
     (recur (str/replace s match replacement) xs))))

;; TODO: only remove affixed digits
(defn no-digits
  "Remove digits from the input string `s`."
  [s]
  (str/replace s #"[0-9]" ""))

(defn char->tone
  "Get the tone digit (0-4) based on a `char-str`, presumably with a diacritic."
  [char-str]
  (when (and (string? char-str) (not-empty char-str))
    (loop [tone 1]
      (cond
        (= 5 tone) 0
        (re-matches (get data/tone-diacritics tone) char-str) tone
        :else (recur (inc tone))))))

(defn- replace-at
  "Like clojure.string/replace, but replaces between index from and to (excl)."
  [s from to replacement]
  (str (subs s 0 from) replacement (subs s to)))

(defn- diacritics->digits*
  "Replaces in `s` based on a `replacements` vector."
  [s replacements]
  (loop [skip          0
         s*            s
         replacements* replacements]
    (if-let [[from syllable tone] (first replacements*)]
      (recur (if tone (inc skip) skip)
             (replace-at s*
                         (+ skip from)
                         (+ skip from (count syllable))
                         (str syllable tone))
             (rest replacements*))
      s*)))

;; https://stackoverflow.com/questions/3262195/compact-clojure-code-for-regular-expression-matches-and-their-position-in-string
;; https://stackoverflow.com/questions/18735665/how-can-i-get-the-positions-of-regex-matches-in-clojurescript
(defn- re-pos
  "Like re-seq, but returns a map of indexes to matches, not a seq of matches."
  [re s]
  #?(:clj  (loop [out {}
                  m   (re-matcher re s)]
             (if (.find m)
               (recur (assoc out (.start m) (.group m)) m)
               out))
     :cljs (let [flags (fn [re]
                           (let [m? (.-multiline re)
                                 i? (.-ignoreCase re)]
                                (str "g" (when m? "m") (when i? "i"))))
                 re     (js/RegExp. (.-source re) (flags re))]
                (loop [out {}]
                      (if-let [m (.exec re s)]
                              (recur (assoc out (.-index m) (first m)))
                              out)))))

(defn diacritics->digits
  "Convert a Pinyin string `s` with tone diacritics into one with tone digits."
  [s]
  (if (string? s)
    (let [s*        (no-diacritics s)
          syllables (re-pos patterns/pinyin-syllable s*)
          original  #(subs s (first %) (+ (first %) (count (second %))))
          diacritic #(re-find #"[^\w]" %)
          tone      (comp char->tone diacritic original)]
      (diacritics->digits* s (map (juxt first second tone) syllables)))
    s))
