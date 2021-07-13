package org.eclipse.dash.licenses.tests.cli;

import org.eclipse.dash.licenses.cli.CommandLineSettings;
import org.eclipse.dash.licenses.cli.GoSumFileReader;
import org.eclipse.dash.licenses.context.DefaultContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

// Todo use mocks...
public class GoSumFileReaderTests {

    private static final String GO_SUM_FILE_CONTENT = "github.com/BurntSushi/toml v0.3.0 h1:WXkYYl6Yr3qBf1K79EBnL4mak0OimBfB0XUf9Vl28OQ=\n" +
            "github.com/BurntSushi/toml v0.3.1/go.mod h1:xHWCNGjB5oqiDr8zfno3MHue2Ht5sIBksp03qcyfWMU=\n" +
            "github.com/BurntSushi/xgb v0.0.0-20160522181843-27f122750802/go.mod h1:IVnqGOEym/WlBOVXweHU+Q+/VP0lqqI8lqeDx9IjBqo=";

    @Test
    public void testShouldParseGoSumFile() {
      InputStream in = new ByteArrayInputStream(GO_SUM_FILE_CONTENT.getBytes());
      GoSumFileReader goSumFileReader = new GoSumFileReader(in, new DefaultContext(CommandLineSettings.getSettings(new String[]{})));
      goSumFileReader.getContentIds();
    }
}
