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

public class Property {
    private String name;
    private String description;
    private String serverExample;
    private String clientExample;

    public Property(String name, String description, String serverExample, String clientExample) {
        this.name = name;
        this.description = description;
        this.serverExample = serverExample;
        this.clientExample = clientExample;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServerExample() {
        return serverExample;
    }

    public void setServerExample(String serverExample) {
        this.serverExample = serverExample;
    }

    public String getClientExample() {
        return clientExample;
    }

    public void setClientExample(String clientExample) {
        this.clientExample = clientExample;
    }
}
