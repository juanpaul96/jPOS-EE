/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.qi.eeuser;

import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.BeanItem;
import org.jpos.ee.*;
import org.jpos.qi.QIHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RolesHelper extends QIHelper {
    protected RolesHelper() {
        super(Role.class);
    }

    @Override
    public Stream getAll(int offset, int limit, Map<String, Boolean> orders) throws Exception {
        List<Role> list = (List<Role>) DB.exec(db -> {
            RoleManager mgr = new RoleManager(db);
            return mgr.getAll(offset,limit,orders);
        });
        return list.stream();
    }

    @Override
    public int getItemCount() throws Exception {
        return (int) DB.exec(db -> {
            RoleManager mgr = new RoleManager(db);
            return mgr.getItemCount();
        });
    }

    @Override
    public String getItemId(Object item) {
        return String.valueOf(((Role)item).getId());
    }

    @Override
    public boolean updateEntity (BeanFieldGroup fieldGroup) throws
            BLException, CloneNotSupportedException {
        BeanItem<Role> old = fieldGroup.getItemDataSource();
        Object oldRole = old.getBean().clone();
        try {
            fieldGroup.commit();
        } catch (FieldGroup.CommitException e) {
            e.printStackTrace();
        }
        BeanItem<Role> item = fieldGroup.getItemDataSource();
        Role r = item.getBean();
        try {
            return (boolean) DB.execWithTransaction( (db) -> {
                db.session().merge(r);
                return addRevisionUpdated(db, getEntityName(),
                        String.valueOf(r.getId()),
                        oldRole,
                        r,
                        new String[]{"id", "name", "permissions"});
            });
        } catch (Exception e) {
            throw new BLException(e.getMessage());
        }
    }

    public SysConfig[] getPermissions () {
        try {
            return (SysConfig[]) DB.exec( (db) -> {
                SysConfigManager mgr = new SysConfigManager(db, "perm.");
                return mgr.getAll();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }

    public Role getRoleByName(String name) {
        try {
            return (Role) DB.exec((db) -> {
                RoleManager mgr = new RoleManager(db);
                return mgr.getRoleByName(name);
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }

    public Validator getNameTakenValidator(final Role selectedR) {
        Validator nameTaken = new Validator() {
            public boolean isValid(Object value) {
                String oldName = selectedR.getName();
                if (oldName!= null)
                    return getRoleByName((String) value) == null || oldName.trim().equals(((String) value).trim());
                else
                    return getRoleByName((String) value) == null;
            }
            public void validate(Object value) throws InvalidValueException {
                if (!isValid(value)) {
                    throw new Validator.InvalidValueException(getApp().getMessage("errorMessage.fieldTaken",value));

                }
            }
        };
        return nameTaken;
    }
}
