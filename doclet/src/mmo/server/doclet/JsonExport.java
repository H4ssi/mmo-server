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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

        File file = new File((outDir == null ? "" : outDir + "/") + "protocol.json");

        List<Message> messages = new LinkedList<>();
        for (ClassDoc c : doc.classes()) {
            if (isProtocolClass(c)) {
                messages.add(new Message(c.simpleTypeName()));
            }
        }

        ObjectMapper m = new ObjectMapper();
        try {
            m.writeValue(file, messages);

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
            if (mmo.server.message.Message.class.getName().equals(t.qualifiedTypeName())) {
                return true;
            }
        }
        return false;
    }
}
