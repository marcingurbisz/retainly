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

## POC Architecture
*(To be defined...)*

## Ideas for Upcoming Features
- Creating flashcards from photos (e.g., a whiteboard or a menu in a foreign language).
- Assisting with hard-to-remember flashcards (leeches) using LLM recommendations:
  - Improving or redefining the flashcard.
  - Adding a related or reverse-translation card.
  - Generating new context/sentences.

## Competitive Analysis
Gemini Deep Research is working... 😁  
... finished - **Language Learning App Competitor Analysis**

# TODO
- Implement first version that adds something via share
  - Check the code from o1
- Register "Retainly Card" in Chrome's context menu
  - Investigate how AnkiDroid did that
