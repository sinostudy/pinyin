# Sino·study Pinyin
[![Sino·study Pinyin](https://img.shields.io/clojars/v/study.sino/pinyin.svg)](https://clojars.org/study.sino/pinyin)

This is a small Clojure/ClojureScript library for converting between different styles of [Hànyǔ Pīnyīn](https://en.wikipedia.org/wiki/Pinyin), the system of romanization used for Standard Chinese in the PRC. It doesn't have any external dependencies.

You can use it to convert back and forth between the standard Pīnyīn representation that uses tone diacritics and the more informal one that uses digits to represent tones. You can also remove tone digits or diacritics entirely, creating a plain ASCII representation useful for normalising syllables when implementing e.g. indexing/search.

Some additional things worth noting:

- The neutral tone is represented as 0 or 5 (or left out entirely) if using digits to write tones.
- Like in many Pīnyīn input methods, a V is treated as a Pīnyīn Ü.
- The non-standard Pīnyīn M is supported (as found in e.g. cc-cedict).
- The very common érhuà-style final R is supported as well.

## Example usage
```clojure
(use 'study.sino.pinyin)

(digits->diacritics "ni3hao3, ni3 shi4 shei2?")  ; => "nǐhǎo, nǐ shì shéi?"
(diacritics->digits "nǐhǎo, nǐ shì shéi?")       ; => "ni3hao3, ni3 shi4 shei2?"

(no-digits "ni3hao3, ni3 shi4 shei2?")           ; => "nihao, ni shi shei?"
(no-diacritics "nǐhǎo, nǐ shì shéi?")            ; => "nihao, ni shi shei?"
```