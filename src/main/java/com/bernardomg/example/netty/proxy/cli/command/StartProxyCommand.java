/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2023 the original author or authors.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bernardomg.example.netty.proxy.cli.command;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.bernardomg.example.netty.proxy.cli.CliWriterProxyListener;
import com.bernardomg.example.netty.proxy.cli.version.ManifestVersionProvider;
import com.bernardomg.example.netty.proxy.server.ProxyListener;
import com.bernardomg.example.netty.proxy.server.ReactorNettyTcpProxyServer;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * Start TCP proxy command.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Command(name = "start", description = "Starts a TCP proxy", mixinStandardHelpOptions = true,
        versionProvider = ManifestVersionProvider.class)
public final class StartProxyCommand implements Runnable {

    /**
     * Debug flag. Shows debug logs.
     */
    @Option(names = { "--debug" }, paramLabel = "flag", description = "Enable debug logs.", defaultValue = "false")
    private Boolean     debug;

    /**
     * Server port.
     */
    @Parameters(index = "0", description = "Proxy server port", paramLabel = "PORT")
    private Integer     port;

    /**
     * Command specification. Used to get the line output.
     */
    @Spec
    private CommandSpec spec;

    /**
     * Target host.
     */
    @Parameters(index = "1", description = "Target host", paramLabel = "TARGET-HOST")
    private String      targetHost;

    /**
     * Target port.
     */
    @Parameters(index = "2", description = "Target port", paramLabel = "TARGET-PORT")
    private Integer     targetPort;

    /**
     * Verbose mode. If active prints info into the console. Active by default.
     */
    @Option(names = { "--verbose" }, paramLabel = "flag", description = "Print information to console.",
            defaultValue = "true", showDefaultValue = Help.Visibility.ALWAYS)
    private Boolean     verbose;

    /**
     * Default constructor.
     */
    public StartProxyCommand() {
        super();
    }

    @Override
    public final void run() {
        final PrintWriter                writer;
        final ReactorNettyTcpProxyServer proxy;
        final ProxyListener              listener;

        if (debug) {
            activateDebugLog();
        }

        if (verbose) {
            // Prints to console
            writer = spec.commandLine()
                .getOut();
        } else {
            // Prints nothing
            writer = new PrintWriter(OutputStream.nullOutputStream(), false, Charset.defaultCharset());
        }

        // Create server
        listener = new CliWriterProxyListener(port, targetHost, targetPort, writer);
        proxy = new ReactorNettyTcpProxyServer(port, targetHost, targetPort, listener);
        proxy.setWiretap(debug);

        // close server
        proxy.start();
    }

    /**
     * Activates debug logs for the application.
     */
    private final void activateDebugLog() {
        Configurator.setLevel("com.bernardomg.example", Level.DEBUG);
        Configurator.setLevel("reactor.netty.tcp", Level.DEBUG);
    }

}
