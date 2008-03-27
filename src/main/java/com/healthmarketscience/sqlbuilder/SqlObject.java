/*
Copyright (c) 2008 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.sqlbuilder;

import java.io.IOException;
import java.util.Collection;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.common.util.AppendeeObject;
import com.healthmarketscience.common.util.StringAppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;



/**
 * Base object which all classes in this facility extend.  It is an
 * AppendeeObject so that the statement building can happen efficiently
 * using the {@link #appendTo} calls.  Additionally, there is a
 * {@link #collectSchemaObjects} method which is used for validation (more details
 * in the method comment).
 *
 * @author James Ahlborn
 */
public abstract class SqlObject extends AppendeeObject
{
  /** SqlObject which represents a <code>?</code> string for generating
      prepared statements. */
  public static final SqlObject QUESTION_MARK = new Expression()
    {
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        app.append("?");
      }
      @Override
      protected void collectSchemaObjects(Collection<Table> tables,
                                      Collection<Column> columns) {}
    };

  /** SqlObject which represents a <code>*</code> string for generating
      "SELECT *" statements. */
  public static final SqlObject ALL_SYMBOL = new SqlObject()
    {
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        app.append("*");
      }
      @Override
      protected void collectSchemaObjects(Collection<Table> tables,
                                      Collection<Column> columns) {}
    };

  /** SqlObject which represents a <code>NULL</code> value string */
  public static final SqlObject NULL_VALUE = new Expression()
    {
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        app.append("NULL");
      }
      @Override
      protected void collectSchemaObjects(Collection<Table> tables,
                                      Collection<Column> columns) {}
    };

  protected SqlObject() {
  }

  /**
   * Creates a SQL string from this object using the given initial size for
   * the AppendableExt buffer and the given SqlContext.  Useful for
   * introducing custom contexts or context settings to the SQL generation.
   * @param size initial size of the output buffer
   * @param context optional custom SqlContext for the SQL generation
   * @return the generated SQL query
   */
  public String toString(int size, SqlContext context) {
    StringAppendableExt app = new StringAppendableExt(size);
    app.setContext(context);
    return app.append(this).toString();
  }  
  
  /**
   * Used during Query.validate() calls to collect the dbschema objects
   * referenced in a query.  Any subclass of this class should add all
   * referenced tables and columns to the appropriate collections.
   * @param tables collection of reference tables
   * @param columns collection of reference columns
   */
  protected abstract void collectSchemaObjects(Collection<Table> tables,
                                           Collection<Column> columns);


}
