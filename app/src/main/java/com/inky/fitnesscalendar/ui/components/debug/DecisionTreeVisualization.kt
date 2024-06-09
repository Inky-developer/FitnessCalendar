package com.inky.fitnesscalendar.ui.components.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.util.decision_tree.DecisionTree
import com.inky.fitnesscalendar.util.removedAt

@Composable
fun DecisionTreeVisualization(
    tree: DecisionTree<*>,
    attributes: List<Pair<String, Map<Any, String>>>
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        when (tree) {
            is DecisionTree.Leaf -> Leaf(tree)
            is DecisionTree.Node -> Node(tree, attributes)
        }
    }
}

@Composable
private fun Leaf(leaf: DecisionTree.Leaf<*>) {
    Text(leaf.value.toString())
}

@Composable
private fun Node(node: DecisionTree.Node<*>, attributes: List<Pair<String, Map<Any, String>>>) {
    val (attributeName, attributeValueNames) = attributes[node.attributeIndex]
    Column {
        for ((attr, child) in node.children.toList().sortedBy { it.first.toString() }) {
            Text("if $attributeName == ${attributeValueNames[attr]}")
            Row(modifier = Modifier.padding(start = 32.dp)) {
                when (child) {
                    is DecisionTree.Leaf -> Leaf(child)
                    is DecisionTree.Node -> Node(
                        child,
                        attributes.removedAt(node.attributeIndex)
                    )
                }
            }
        }
        Text("else ${node.default}")
    }
}