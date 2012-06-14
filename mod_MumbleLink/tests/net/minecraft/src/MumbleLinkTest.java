/*
 mod_MumbleLink - Positional Audio Communication for Minecraft with Mumble
 Copyright 2012 zsawyer (http://sourceforge.net/users/zsawyer)

 This file is part of mod_MumbleLink
 (http://sourceforge.net/projects/modmumblelink/).

 mod_MumbleLink is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 mod_MumbleLink is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with mod_MumbleLink.  If not, see <http://www.gnu.org/licenses/>.

 */
package net.minecraft.src;

import java.io.File;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 *
 * @author zsawyer
 */
@RunWith(JMock.class)
@Ignore
public class MumbleLinkTest {

    public static final String FIXTURES_BASE_DIR = "../../../mod_MumbleLink/tests/fixtures/";
    public static final String DIRECTORY_WITH_WINDOWS_NATIVES = FIXTURES_BASE_DIR + "windowsNatives";
    public static final String DIRECTORY_WITH_LINUX_NATIVES = FIXTURES_BASE_DIR + "linuxNatives";
    public static final String DIRECTORY_WITHOUT_NATIVES = FIXTURES_BASE_DIR + "noNatives";
    public static final String DIRECTORY_WITH_OBFUSCATED_NATIVES = FIXTURES_BASE_DIR + "obfuscatedNatives";
    public static final float IGNORABLE_TICK = 0.0F;
    public static final String MOD_CLASS_NAME = mod_MumbleLink.class.getName();
    public static Mockery mockContext;
    public static Minecraft mockedGameInstance;
    public mod_MumbleLink testSubjectInstance;

    @BeforeClass
    public static void setUpClass() throws Exception {
        mockContext = createMockContext();
        mockedGameInstance = createGameInstance(mockContext);
    }

    private static Mockery createMockContext() {
        Mockery context = new JUnit4Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);
        return context;
    }

    private static Minecraft createGameInstance(Mockery mockContext) {
        return mockContext.mock(Minecraft.class);
    }

    @Before
    public void setUp() {
        setUpModInstance(DIRECTORY_WITH_WINDOWS_NATIVES);

        try {
            testSubjectInstance = tryModInstantiation();
        } catch (Throwable err) {
            fail("Setup probably needs refactoring.");
        }
    }

    protected void setUpModInstance(String executionDirectory) {
        try {
            setGameExecutionDirectory(executionDirectory);
        } catch (Throwable error) {
            fail("This setup broke. Compare with MCP's Start.main() and possibly update.");
        }
    }

    private void setGameExecutionDirectory(String executionDirectory) throws Exception {
        Field f = Minecraft.class.getDeclaredField("minecraftDir");
        Field.setAccessible(new Field[]{f}, true);
        f.set(null, new File(executionDirectory));
    }

    protected mod_MumbleLink tryModInstantiation() throws InstantiationError {
        try {
            Class<?> modClass = tryModClassLoading();
            return (mod_MumbleLink) modClass.newInstance();
        } catch (Throwable initCause) {
            throw createInstantiationError(initCause);
        }
    }

    protected Class<?> tryModClassLoading() throws InstantiationError {
        if (isModClassLoaded()) {
            throw createInstantiationError("class was already loaded");
        }
        try {
            Class<?> loadedClass = InterrogativeClassLoader.getInstance().loadClass(MOD_CLASS_NAME);
            assertEquals(true, isModClassLoaded());
            return loadedClass;
        } catch (AssertionError err) {
            throw err;
        } catch (Throwable initCause) {
            throw createInstantiationError(initCause);
        }
    }

    private static boolean isModClassLoaded() {
        return InterrogativeClassLoader.getInstance().isClassLoaded(MOD_CLASS_NAME);
    }

    protected InstantiationError createInstantiationError(Throwable initCause) {
        InstantiationError aggregateError = new InstantiationError();
        aggregateError.initCause(initCause);
        return aggregateError;
    }

    protected InstantiationError createInstantiationError(String reason) {
        return createInstantiationError(new Exception(reason));
    }

    protected static void unloadModClass() {
        while (isModClassLoaded()) {
            System.gc();
        }
    }
}