/*
 * Copyright 2016 Florian Hassanen
 *
 * This file is part of mmo-server.
 *
 * mmo-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * mmo-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with mmo-server.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package mmo.server.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import mmo.server.message.Message;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by flori on 22.04.2016.
 */
public class JsonExport {
    public static boolean start(RootDoc doc) {
        String outDir = null;

        for (String[] ops : doc.options()) {
            if ("-d".equals(ops[0])) {
                outDir = ops[1];
            }
        }

        String file = (outDir == null ? "" : (outDir + "/")) + "protocol.json";

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("[");
            boolean first = true; // TODO use proper json library
            for (ClassDoc c : doc.classes()) {
                if (isProtocolClass(c)) {
                    if (!first) {
                        fw.write(",");
                    } else {
                        first = false;
                    }
                    fw.write('"');
                    fw.write(c.qualifiedTypeName());
                    fw.write('"');
                }
            }
            fw.write("]");
            return true;
        } catch (IOException e) {
            doc.printError(e.getMessage());
            return false;
        }
    }

    public static int optionLength(String option) {
        if ("-d".equals(option)) {
            return 2;
        }
        return 0;
    }

    private static boolean isProtocolClass(ClassDoc c) {
        for (Type t : c.interfaceTypes()) {
            if (Message.class.getName().equals(t.qualifiedTypeName())) {
                return true;
            }
        }
        return false;
    }
}
