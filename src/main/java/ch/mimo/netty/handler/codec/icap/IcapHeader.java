/*******************************************************************************
 * Copyright (c) 2011 Michael Mimo Moratti.
 *  
 * Michael Mimo Moratti licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package ch.mimo.netty.handler.codec.icap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Icap Headers
 * 
 * This class provides a linked list implementation in order to store Icap headers.
 * 
 * @author Michael Mimo Moratti (mimo@mimo.ch)
 *
 */
public final class IcapHeader {
	
	private Entry base;
	private Entry head;
	
	/**
	 * The most common Icap Header names.
	 * 
	 * @author Michael Mimo Moratti (mimo@mimo.ch)
	 *
	 */
	public static final class Names {
        /**
         * {@code "Cache-Control"}
         */
		public static final String CACHE_CONTROL = "Cache-Control";
        /**
         * {@code "Connection"}
         */
		public static final String CONNECTION = "Connection";
        /**
         * {@code "Date"}
         */
		public static final String DATE = "Date";	
        /**
         * {@code "Expires"}
         */
		public static final String EXPIRES = "Expires";
        /**
         * {@code "Pragma"}
         */
		public static final String PRAGMA = "Pragma";
        /**
         * {@code "Trailer"}
         */
		public static final String TRAILER = "Trailer";
        /**
         * {@code "Upgrade"}
         */
		public static final String UPGRADE = "Upgrade";
        /**
         * {@code "Encapsulated"}
         */
		public static final String ENCAPSULATED = "Encapsulated";
        /**
         * {@code "Authorization"}
         */
		public static final String AUTHORIZATION = "Authorization";
        /**
         * {@code "Allow"}
         */
		public static final String ALLOW = "Allow";
        /**
         * {@code "From"}
         */
		public static final String FROM = "From";
        /**
         * {@code "Host"}
         */
		public static final String HOST = "Host";
        /**
         * {@code "Referer"}
         */
		public static final String REFERER = "Referer";
        /**
         * {@code "User-Agent"}
         */
		public static final String USER_AGENT = "User-Agent";
        /**
         * {@code "Preview"}
         */
		public static final String PREVIEW = "Preview";
		
		/**
		 * {@code "ISTag"}
		 */
		public static final String ISTAG = "ISTag";
	}
	
	public IcapHeader() {
	}
	
	public void clearHeaders() {
		base = null;
		head = null;
	}
	
	/**
	 * Adds a header key,value combination to the list
	 * The existence of such a header name will have no impact. The header is simply added
	 * to the end of the linked list. 
	 * 
	 * @param name Icap message header name
	 * @param value Icap message header value. Can also be null
	 */
	public void addHeader(String name, Object value) {
		Entry entry = new Entry(name,value);
		if(base == null) {
			base = entry;
			head = entry;
		} else {
			Entry currentHead = head;
			head = entry;
			entry.before = currentHead;
			currentHead.after = entry;
		}
	}
	
	/**
	 * Sets one header at the end of the list.
	 * Headers with the same name that are already in the list will be removed first!
	 * 
	 * @param name Icap message header name
	 * @param value Icap message header value. Can also be null
	 */
	public void setHeader(String name, Object value) {
		removeHeader(name);
		addHeader(name,value);
	}
	
	/**
	 * Sets one header with many values.
	 * Headers with the same name that are already in the list will be removed first!
	 * 
	 * @param name Icap message header name
	 * @param values Icap message header value. Can also be null
	 */
	public void setHeader(String name, Iterable<?> values) {
		removeHeader(name);
		for(Object value : values) {
			addHeader(name,value);
		}
	}
	
	/**
	 * retrieves a header value from the list.
	 * If no header exists with the given name null is returned.
	 *
	 * If there are multiple headers with the same name only the first occurence in the
	 * list is returned.
	 * 
	 * @param name Icap message header name
	 * @return String value or null
	 */
	public String getHeader(String name) {
		Entry entry = base;
		while(entry != null) {
			if(identicalKeys(entry.getKey(),name)) {
				return entry.getValue();
			}
			entry = entry.after;
		}
		return null;
	}
	
	/**
	 * retrieves all values for one header name.
	 * If no header exists with that given name an empty set is returned.
	 * 
	 * @param name Icap message header name
	 * @return Set of values from all headers with the same name, or empty set.
	 */
	public Set<String> getHeaders(String name) {
		Set<String> values = new LinkedHashSet<String>();
		Entry entry = base;
		while(entry != null) {
			if(identicalKeys(entry.getKey(),name)) {
				values.add(entry.getValue());
			}
			entry = entry.after;
		}
		return values;
	}
	
	/**
	 * retrieval method for all headers that are currently in this list.
	 * 
	 * @return Set of Map Entry instances.
	 */
	public Set<Map.Entry<String, String>> getHeaders() {
		Set<Map.Entry<String, String>> headers = new LinkedHashSet<Map.Entry<String,String>>();
		Entry entry = base;
		while(entry != null) {
			headers.add(entry);
			entry = entry.after;
		}
		return headers;
	}
	
	/**
	 * check method to validate if a certain header does exists in the list.
	 * 
	 * @param name Icap message header name
	 * @return boolean true if the header exists.
	 */
	public boolean containsHeader(String name) {
		return getHeader(name) != null;
	}
	
	/**
	 * removes all headers with the same name from the list.
	 * 
	 * @param name Icap message header name
	 */
	public void removeHeader(String name) {
		if(base.after == null) {
			base = null;
			return;
		}
		Entry entry = base.after;
		while(entry != null) {
			if(identicalKeys(entry.getKey(),name)) {
				Entry before = entry.before;
				Entry after = entry.after;
				before.after = after;
				after.before = before;
				entry = after;
			} else {
				entry = entry.after;
			}
		}
		if(identicalKeys(base.getKey(),name)) {
			base = base.after;
			base.before = null;
		}
	}
	
	/**
	 * retrieval method for all header names.
	 * this list is unique. If a header name has two entries the name is returned only once.
	 * 
	 * @return unique set with all header names in the list.
	 */
	public Set<String> getHeaderNames() {
		Set<String> names = new LinkedHashSet<String>();
		Entry entry = base;
		while(entry != null) {
			names.add(entry.getKey());
			entry = entry.after;
		}
		return names;
	}
	
	/**
	 * Convenience method to retrieve the @see {@link Integer} value from a Icap Preview header.
	 * If the header does not exist the value 0 is returned.
	 * If the header value cannot be parsed into a valid integer a @see {@link IcapDecodingError} is thrown.
	 * 
	 * @return int value of preview header.
	 */
	public int getPreviewHeaderValue() {
		String value = getHeader(Names.PREVIEW);
		int result = 0;
		try {
			if(value != null) {
				result = Integer.parseInt(value);
			}
		} catch(NumberFormatException nfe) {
			throw new IcapDecodingError("Unable to understand the preview amount value [" + value + "]");
		}
		return result;
	}
	
	private boolean identicalKeys(String key1, String key2) {
		if(key1 != null & key2 != null) {
			return key1.equalsIgnoreCase(key2);
		}
		return false;
	}

	private static final class Entry implements Map.Entry<String, String> {
		
		private String key;
		private String value;
		
		private Entry before, after;
		
		Entry(String key, Object value) {
			IcapCodecUtil.validateHeaderName(key);
			this.key = key;
			if(value != null) {
				this.value = value.toString();
				IcapCodecUtil.validateHeaderValue(this.value);
			}
		}
		
		public String getKey() {
			return key;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public String setValue(String value) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(String name : getHeaderNames()) {
			builder.append("[" + name + "] = [" + getHeaders(name) + "]");
		}
		return builder.toString();
	}
}
