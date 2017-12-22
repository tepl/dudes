package com.tearulez.dudes.server;

import com.badlogic.gdx.math.Vector2;
import com.tearulez.dudes.common.snapshot.Point;
import com.tearulez.dudes.common.snapshot.Wall;
import com.tearulez.dudes.server.engine.GameModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.tearulez.dudes.server.Assertions.require;

public class SvgMap {
    private final Document doc;
    private final XPath xpath = XPathFactory.newInstance().newXPath();

    public SvgMap(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(file);
    }

    public List<Wall> getWalls() {
        List<Wall> walls = new ArrayList<>();
        addPolygonWalls(walls);
        addRectangleWalls(walls);
        return walls;
    }

    private void addPolygonWalls(List<Wall> walls) {
        NodeList nodes = getNodesFromXPath("//polygon");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String pointsString = getAttributeValue(node, "points");
            List<Point> unscaledPoints = parsePoints(pointsString);
            Node transformAttr = node.getAttributes().getNamedItem("transform");
            walls.add(createWall(unscaledPoints, transformAttr));
        }
    }

    private void addRectangleWalls(List<Wall> walls) {
        NodeList nodes = getNodesFromXPath("//rect");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            float height = getFloatAttribute(node, "height");
            float width = getFloatAttribute(node, "width");
            float x = getFloatAttribute(node, "x");
            float y = getFloatAttribute(node, "y");
            List<Point> unscaledPoints = Arrays.asList(
                    Point.create(x, y),
                    Point.create(x + width, y),
                    Point.create(x + width, y + height),
                    Point.create(x, y + height)
            );
            Node transformAttr = node.getAttributes().getNamedItem("transform");
            walls.add(createWall(unscaledPoints, transformAttr));
        }
    }

    private NodeList getNodesFromXPath(String expression) {
        try {
            XPathExpression expr = xpath.compile(expression);
            return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private Wall createWall(List<Point> unscaledPoints, Node transformAttr) {
        List<Point> points = unscaledPoints;
        if (transformAttr != null) {
            Rotation rotation = parseRotation(transformAttr.getNodeValue());
            points = points.stream()
                    .map(rotation::rotate)
                    .collect(Collectors.toList());
        }
        List<Point> result = points.stream()
                .map(this::scalePoint)
                .collect(Collectors.toList());
        require(result.size() <= 8, "polygon should not have more than 8 vertices");
        return Wall.create(Point.create(0, 0), result);
    }

    private Rotation parseRotation(String string) {
        require(string.startsWith("rotate"), "string should start with \"rotate\"");
        String[] args = string.substring("rotate(".length(), string.length() - 1).split(",");
        List<Float> parsedArgs = Arrays.stream(args).map(Float::valueOf).collect(Collectors.toList());
        return new Rotation(parsedArgs.get(0), parsedArgs.get(1), parsedArgs.get(2));
    }

    private Point scalePoint(Point point) {
        Point playerCenter = parsePlayerCenter();
        float playerRadius = parsePlayerRadius();
        float scaleFactor = GameModel.PLAYER_CIRCLE_RADIUS / playerRadius;
        return Point.create(
                (point.x - playerCenter.x) * scaleFactor,
                -(point.y - playerCenter.y) * scaleFactor /*y-axis inversion*/
        );
    }

    private float parsePlayerRadius() {
        Node node = getPlayerCircleNode();
        float rx = getFloatAttribute(node, "rx");
        float ry = getFloatAttribute(node, "ry");
        require(rx == ry, "player ellipse should be a circle");
        return rx;
    }

    private Point parsePlayerCenter() {
        Node node = getPlayerCircleNode();
        return Point.create(
                getFloatAttribute(node, "cx"),
                getFloatAttribute(node, "cy")
        );
    }

    private Float getFloatAttribute(Node node, String name) {
        return Float.valueOf(getAttributeValue(node, name));
    }

    private String getAttributeValue(Node item, String name) {
        return item.getAttributes().getNamedItem(name).getNodeValue();
    }

    private Node getPlayerCircleNode() {
        NodeList nodes = getNodesFromXPath("//ellipse");
        require(nodes.getLength() == 1, "there must be one and only one player ellipse");
        return nodes.item(0);
    }

    private List<Point> parsePoints(String pointsString) {
        List<Point> points = new ArrayList<>();
        String[] pointStrings = pointsString.split(" ");
        for (String pointString : pointStrings) {
            String[] xAndY = pointString.split(",");
            points.add(Point.create(Float.valueOf(xAndY[0]), Float.valueOf(xAndY[1])));
        }
        return points;
    }

    private static class Rotation {
        final float angleInDegrees;
        final float centerX;
        final float centerY;

        Rotation(float angleInDegrees, float centerX, float centerY) {
            this.angleInDegrees = angleInDegrees;
            this.centerX = centerX;
            this.centerY = centerY;
        }

        Point rotate(Point point) {
            Vector2 p = new Vector2(point.x, point.y);
            Vector2 c = new Vector2(centerX, centerY);
            Vector2 diff = p.sub(c);
            diff.rotate(angleInDegrees);
            Vector2 rotated = diff.add(c);
            return Point.create(rotated.x, rotated.y);
        }
    }
}
