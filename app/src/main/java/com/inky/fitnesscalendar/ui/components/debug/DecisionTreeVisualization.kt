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
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.util.decision_tree.DecisionTree
import com.inky.fitnesscalendar.util.removedAt

@Composable
fun DecisionTreeVisualization(
    tree: DecisionTree<ActivityType>,
    attributes: List<Pair<String, (Any?) -> String>>
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
private fun Leaf(leaf: DecisionTree.Leaf<ActivityType>) {
    Text(leaf.value?.name ?: "null")
}

@Composable
private fun Node(
    node: DecisionTree.Node<ActivityType>,
    attributes: List<Pair<String, (Any?) -> String>>
) {
    val (attributeName, getAttributeName) = attributes[node.attributeIndex]
    Column {
        for ((attr, child) in node.children.toList().sortedBy { it.first.toString() }) {
            Text("if $attributeName == ${getAttributeName(attr)}")
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
        Text("else ${node.default?.name ?: "null"}")
    }
}