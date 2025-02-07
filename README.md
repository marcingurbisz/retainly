# Retainly

## Core Problem / Pain Points
- People learning a new language often encounter new words or phrases in their daily reading (e.g., browsing the web) but don’t capture them effectively.
- Traditional flashcard creation can be time-consuming and manual.
- Learners often struggle with “leeches” (cards they keep forgetting).

## Solution
- A lightweight and frictionless way to create flashcards directly from any webpage or photo.
- Use Large Language Models (LLMs) to assist in generating and refining flashcards (e.g., translations, usage examples, images).
- Provide robust spaced repetition (SRS) on mobile to ensure proper review and long-term retention.

## Scope of POC
### Chrome Plugin
- Highlight text on an English webpage and quickly generate a flashcard (translating into Polish).
- Leverage an LLM to prepare flash card.

### Android App
- Perform daily SRS reviews of created flashcards.
- Register an additional browser action - **"Retainly Card"** - that works like in the browser plugin.

## Ideas for Upcoming Features
- Creating flashcards from photos (e.g., a whiteboard or a menu in a foreign language).
- Assisting with hard-to-remember flashcards (leeches) using LLM recommendations:
  - Improving or redefining the flashcard.
  - Adding a related or reverse-translation card.
  - Generating new context/sentences.

# TODO
- Add an automatic translation feature using LLM
  - Check Windsurf, Cursor, and GitHub Copilot when implementing this feature.
- Create a screen displaying a list of cards and the next recall.
- Allow users to highlight an entire sentence, then select specific words they don't know, with LLM suggesting corresponding cards.
- Implement a deck backup feature.
  - Consider cloud synchronization, but it may be too early. A simpler backup solution should be sufficient at this stage.
- Add text-to-speech functionality for English words.
  - When a card is added, a text-to-speech service should generate and attach an audio file.
- Register "Retainly Card" in Chrome's context menu instead of using the share option.
  - Investigate how AnkiDroid implements this feature.