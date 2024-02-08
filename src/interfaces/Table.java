package interfaces;
import java.util.ArrayList;

public class Table implements TableSchema {
        private ArrayList<AttributeSchema> tableAttributes;
        private String tableName;
        private int tableId;

        public Table(int id, String name, ArrayList<AttributeSchema> attributes) {
                this.tableId = id;
                this.tableName = name;
                this.tableAttributes = attributes;
        }

        @Override
        public int getTableId() {
                return tableId;
        }

        @Override
        public String getTableName() {
                return tableName;
        }

        @Override
        public ArrayList<AttributeSchema> getAttributeSchema() {
                return tableAttributes;
        }

        @Override
        public AttributeSchema getAttributeSchema(String attributeName) {
                for (int i = 0; i < this.tableAttributes.size(); i++) {
                        if (tableAttributes.get(i).getAttributeName() == attributeName)
                                return tableAttributes.get(i);

                }
                return null;
        }

        @Override
        public void addAttributeSchema(AttributeSchema attributeSchema) {
                this.tableAttributes.add(attributeSchema);
        }

        @Override
        public void removeAttributeSchema(String attributeName) {
                // removes a attribute if it belongs to the table. Otherwise, does nothing.
                int removeIndex = -1;
                for (int i = 0; i < this.tableAttributes.size(); i++) {
                        if (tableAttributes.get(i).getAttributeName() == attributeName) {
                                removeIndex = i;
                                break;
                        }
                }
                if (removeIndex == -1) {
                        return;
                }
                this.tableAttributes.remove(removeIndex);
        }
}
