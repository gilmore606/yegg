# Yegg

A MUD server written in Kotlin, inspired by Pavel Curtis's MOO server.

Like MOO, Yegg aspires to be a generalized server for creating MUD games, rather than implementing any particular MUD style or rules.  It incorporates a compiler for its internal scripting language (YeggCode), a single-threaded task scheduler, and an extensible command parser.  The actual structure and behavior of the MUD world are defined in YeggCode by the programmer(s).

## Differences from MOO

If you're familiar with MOO, Yegg differs in a few key aspects:

- YeggCode is much more flexible than MOOcode.  It includes flexible formatting, a C-like block structure, anonymous functions, maps, and other modern-ish features.
- Yegg worlds have two kinds of entities: Objects, and Traits.  Traits serve the function of fertile parent objects in MOO; they hold verbs and properties defining behavior, but they do not actually 'exist' in the world as Objects.
- Yegg objects support multi-inheritance; an Object can have multiple Traits from which it gets behavior, and any Trait can have multiple parents as well.
- Yegg code is not interpreted per se, but is compiled to an intermediate stack-machine language for speed.  Unlike MOO, the original source code text is kept.

## Getting started

## YeggCode
