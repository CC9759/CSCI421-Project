package BPlusTree;

public class BPlusTree {
        private int bucketSize; // Size of the b+ tree to get size of nodes and separation
        private TreeNode root;

        public BPlusTree(int size) {
                this.bucketSize = size;
                this.root = new TreeNode(size);
                this.root.isLeaf = true;
        }

        /**
         * function to insert a value in the B+ Tree
         * 
         * @param value the specified value that will be insert
         * @return a boolean. true if the value was succesfully added to the tree.
         *         otherwise, it returns false
         */
        public boolean insert(int value) {
                return root.insert(value);
        }

        public boolean delete(int value) {
                return root.delete(value);
        }

        public TreeNode find(int value) {
                return root.find(value);
        }

        public int getTreeSize() {
                return bucketSize;
        }

        /**
         * prints the tree. gets each
         */
        public void printTree() {
                TreeNode currentNode = root;
                while (currentNode.isLeaf == false) {
                        TreeNode nextNode = currentNode.keys.get(0);
                        while (currentNode.nextNode != null) {
                                currentNode.printValues();
                                System.out.print(currentNode.nextNode == null ? "\n" : " -> ");
                                currentNode = currentNode.nextNode;
                        }
                        currentNode = nextNode;
                }

                while (currentNode != null) {
                        currentNode.printValues();
                        System.out.print(currentNode.nextNode == null ? "\n" : " -> ");
                        currentNode = currentNode.nextNode;
                }
        }

        public static void main(String[] args) {
                BPlusTree tree = new BPlusTree(5);
                System.out.println(tree.insert(12));
                System.out.println(tree.insert(10));
                System.out.println(tree.insert(11));
                System.out.println(tree.insert(12)); // repeated value
                System.out.println(tree.insert(32));
                System.out.println(tree.insert(15));
                System.out.println(tree.insert(1));

                tree.printTree();
        }
}
