/*
 * $Id: Category.java 556 2007-08-15 19:33:53Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package hfu.parsers.cfg.pep;


/**
 * A category in a grammar. Categories are the atomic subparts that make up
 * {@link Rule grammar rules}.
 * <p>
 * Categories can either be <em>terminal</em> or <em>non-terminal</em>. A
 * terminal category is one from which no further categories can be derived,
 * while non-terminal categories can yield a series of other categories when
 * they occur as the {@link Rule#getLeft() left-hand side} of a rule. If a
 * category is created by specifying <em>only</em> its name, this class's 
 * {@link Category#Category(String) corresponding constructor} assumes that
 * the category is non-terminal.
 * <p>
 * Once created, categories are immutable and have no <code>setXxx</code>
 * methods. This ensures that, once loaded in a grammar, a category will
 * remain as it was when created.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 556 $
 * @see Rule
 */
public class Category {
	String name;
	boolean terminal;
	
	/**
	 * Special start category for seeding Earley parsers. 
	 */
	public static final Category START = new Category("<start>", false) {
		/**
		 * Overrides {@link Category#equals(Object)} to compare using the
		 * <code>==</code> operator (since there is only ever one start
		 * category).
		 */
		@Override
		public boolean equals(Object obj) {
			return (this == obj);
		}
	};
	
	/**
	 * Creates a new non-terminal category with the specified name.
	 * @see Category#Category(String, boolean)
	 */
	public Category(String name) {
		this(name, false);
	}

	/**
	 * Creates a new category <code>name</code> with the specified terminal
	 * status.
	 * @param name The name for this category.
	 * @param terminal Whether or not this category is a terminal.
	 * @throws IllegalArgumentException If <code>name</code> is
	 * <code>null</code> or zero-length.
	 */
	public Category(String name, boolean terminal) {
		if(!terminal && (name == null || name.length() == 0)) {
			throw new IllegalArgumentException(
					"empty name specified for category");
		}
		
		this.name = name;
		this.terminal = terminal;
	}
	
	/**
	 * Gets the name of this category.
	 * @return The value specified for this category's name when it was
	 * constructed.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the terminal status of this category.
	 * @return The terminal status specified for this category upon 
	 * construction.
	 */
	public boolean isTerminal() {
		return terminal;
	}

	/**
	 * Tests whether this category is equal to another.
	 * @return <code>true</code> iff the specified object is an instance
	 * of <code>Category</code> and its name and terminal status are equal
	 * to this category's name and terminal status.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Category) {
			Category oc = (Category)obj;
			return (oc != Category.START && 
					terminal == oc.terminal && name.equals(oc.name));
		}

		return false;
	}

	/**
	 * Computes a hash code for this category based on its name and terminal
	 * status.
	 */
	@Override
	public int hashCode() {
		return (31 * name.hashCode() * Boolean.valueOf(terminal).hashCode());
	}

	/**
	 * Gets a string representation of this category.
	 * @return The value of this category's name.
	 */
	@Override
	public String toString() {
		return (name.length() == 0) ? "<empty>" : name;
	}
}
