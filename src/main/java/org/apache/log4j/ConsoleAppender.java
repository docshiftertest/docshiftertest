/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;

/**
 * NOP Console-appender class that extends the log4j-1.2-api bridge to include a constructor that DFC (which
 * hardcodes to a very old log4j1 lib) calls.
 */
public class ConsoleAppender extends AppenderSkeleton
{

	/**
	 * This is the extra constructor not contained within the log4j bridge that is necessary for the logger in DFC to
	 * do its thing.
	 * @param layout
	 */
	public ConsoleAppender(Layout layout) {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean requiresLayout()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void append(final LoggingEvent theEvent)
	{
	}

}