package org.apache.maven.plugin.surefire.extensions;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.extensions.CloseableDaemonThread;
import org.apache.maven.surefire.extensions.CommandReader;
import org.apache.maven.surefire.extensions.EventHandler;
import org.apache.maven.surefire.extensions.ForkChannel;
import org.apache.maven.surefire.extensions.util.CountdownCloseable;
import org.apache.maven.surefire.extensions.util.LineConsumerThread;
import org.apache.maven.surefire.extensions.util.StreamFeeder;

import javax.annotation.Nonnull;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * The main purpose of this class is to bind the
 * {@link #bindCommandReader(CommandReader, WritableByteChannel) command reader} reading the commands from
 * {@link CommandReader}, serializing them and writing the stream to the
 * {@link WritableByteChannel sub-process}. It binds the
 * {@link #bindEventHandler(EventHandler, CountdownCloseable, ReadableByteChannel) event handler} deserializing
 * a received event and sends the event object to the {@link EventHandler event handler}.
 */
final class LegacyForkChannel extends ForkChannel
{
    protected LegacyForkChannel( int forkChannelId )
    {
        super( forkChannelId );
    }

    @Override
    public void connectToClient()
    {
    }

    @Override
    public String getForkNodeConnectionString()
    {
        return "pipe://" + getForkChannelId();
    }

    @Override
    public boolean useStdOut()
    {
        return true;
    }

    @Override
    public CloseableDaemonThread bindCommandReader( @Nonnull CommandReader commands,
                                                    WritableByteChannel stdIn )
    {
        return new StreamFeeder( "std-in-fork-" + getForkChannelId(), stdIn, commands );
    }

    @Override
    public CloseableDaemonThread bindEventHandler( @Nonnull EventHandler eventHandler,
                                                   @Nonnull CountdownCloseable countdownCloseable,
                                                   ReadableByteChannel stdOut )
    {
        // todo develop Event and EventConsumerThread, see the algorithm in ForkedChannelDecoder#handleEvent()
        return new LineConsumerThread( "fork-" + getForkChannelId() + "-event-thread-", stdOut,
            eventHandler, countdownCloseable );
    }

    @Override
    public void close()
    {
    }
}
