(ns study.sino.pinyin-test
  (:require [clojure.test :refer [deftest is are testing]]
            [study.sino.pinyin :refer [with-umlaut
                                       with-diacritic
                                       diacritic-index
                                       digit->diacritic
                                       digits->diacritics
                                       diacritics->digits
                                       char->tone
                                       no-digits
                                       no-diacritics]]))

(deftest test-umlaut
  (testing "umlaut"
    (is (= (with-umlaut "VvÜü") "ÜüÜü"))))

;; only tests a single char for now!
(deftest test-diacritic
  (testing "diacritic"
    (testing "added to characters?"
      (are [x y] (= x y)
        \a (with-diacritic 0 \a)
        \ā (with-diacritic 1 \a)
        \á (with-diacritic 2 \a)
        \ǎ (with-diacritic 3 \a)
        \à (with-diacritic 4 \a)
        \a (with-diacritic 5 \a)
        \A (with-diacritic 0 \A)
        \Ā (with-diacritic 1 \A)
        \Á (with-diacritic 2 \A)
        \Ǎ (with-diacritic 3 \A)
        \À (with-diacritic 4 \A)
        \A (with-diacritic 5 \A)))
    (testing "tone out of range?"
      (is (thrown? IndexOutOfBoundsException (with-diacritic 6 \a))))
    (testing "string instead of char?"
      (is (nil? (with-diacritic 1 "1"))))))

(deftest test-diacritic-index
  (testing "diacritic-index"
    (testing "a-rule"
      (is (= (diacritic-index "ao1") 0))
      (is (= (diacritic-index "lang4") 1))
      (is (= (diacritic-index "quan") 2)))
    (testing "e-rule"
      (is (= (diacritic-index "eng") 0))
      (is (= (diacritic-index "heng1") 1))
      (is (= (diacritic-index "zheng") 2)))
    (testing "ou-rule"
      (is (= (diacritic-index "ou") 0))
      (is (= (diacritic-index "tou2") 1))
      (is (= (diacritic-index "zhou") 2)))
    (testing "general rule"
      (is (= (diacritic-index "e") 0))
      (is (= (diacritic-index "eng") 0))
      (is (= (diacritic-index "long2") 1))
      (is (= (diacritic-index "lan") 1))
      (is (= (diacritic-index "kuo4") 2)))
    (testing "mixed case"
      (is (= (diacritic-index "WANG") 1))
      (is (= (diacritic-index "lI0") 1))
      (is (= (diacritic-index "Qu4") 1)))
    (testing "undefined cases (returns nil)"
      (is (nil? (diacritic-index nil)))
      (is (nil? (diacritic-index "")))
      (is (nil? (diacritic-index "4")))
      (is (nil? (diacritic-index [1 2 3])))
      (is (nil? (diacritic-index {:foo :bar})))
      (is (nil? (diacritic-index {:foo :bar}))))))

(deftest test-digit->diacritic
  (testing "digit->diacritic"
    (testing "converts properly?"
      (is (= (digit->diacritic "long3") "lǒng"))
      (is (= (digit->diacritic "er2") "ér")))
    (testing "exceptions"
      (is (thrown? NumberFormatException (digit->diacritic "long")))
      (is (thrown? ClassCastException (digit->diacritic [1 2 3]))))))

(deftest test-digits->diacritics
  (testing "digits->diacritics"
    (testing "converts properly?"
      (is (= (digits->diacritics "ni3hao3, ni3 shi4 shei2?") "nǐhǎo, nǐ shì shéi?"))
      (is (= (digits->diacritics "long") "long"))
      (is (= (digits->diacritics "") "")))
    (testing "non-strings"
      (is (= (digits->diacritics []) []))
      (is (= (digits->diacritics [1 2 3]) [1 2 3]))
      (is (= (digits->diacritics 0) 0))
      (is (= (digits->diacritics \a) \a)))))

(deftest test-diacritics->digits
  (testing "diacritics->digits"
    (testing "converts properly?"
      (is (= (diacritics->digits "nǐhǎo, nǐ shì shéi?") "ni3hao3, ni3 shi4 shei2?"))
      (is (= (diacritics->digits "long") "long"))
      (is (= (diacritics->digits "") "")))
    (testing "non-strings"
      (is (= (diacritics->digits []) []))
      (is (= (diacritics->digits [1 2 3]) [1 2 3]))
      (is (= (diacritics->digits 0) 0))
      (is (= (diacritics->digits \a) \a)))))

(deftest test-char->tone
  (testing "char->tone"
    (testing "converts properly?"
      (is (= (char->tone "e") 0))
      (is (= (char->tone "ā") 1))
      (is (= (char->tone "é") 2))
      (is (= (char->tone "ǐ") 3))
      (is (= (char->tone "ì") 4)))
    (testing "non-strings"
      (is (nil? (char->tone "")))
      (is (nil? (char->tone \a))))))

(deftest test-no-digits
  (testing "no-digits"
    (testing "converts properly?"
      (is (= (no-digits "ni3hao3, ni3 shi4 shei2?") "nihao, ni shi shei?")))
    (testing "non-strings"
      (is (= (no-digits \a) "a")))))

(deftest test-no-diacritics
  (testing "no-diacritics"
    (testing "converts properly?"
      (is (= (no-diacritics "nǐhǎo, nǐ shì shéi?") "nihao, ni shi shei?")))
    (testing "non-strings"
      (is (= (no-diacritics \a) "a")))))
