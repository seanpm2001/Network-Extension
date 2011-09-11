package org.nlogo.extensions.network

import org.nlogo.api.{
  DefaultClassManager, PrimitiveManager, Agent, AgentSet, Argument, Context, DefaultReporter,
  ExtensionException, I18N, Primitive, Syntax, Turtle }

class NetworkExtension extends DefaultClassManager {
  override def load(primManager: PrimitiveManager) {
    primManager.addPrimitive("link-distance", LinkDistance)
    primManager.addPrimitive("extended-link-neighbors", ExtendedLinkNeighbors)
    primManager.addPrimitive("mean-path-length", MeanPathLength)
    primManager.addPrimitive("path-turtles", PathTurtles)
    primManager.addPrimitive("path-links", PathLinks)
  }
}

trait Helpers {
  def requireTurtleset(agents: AgentSet) {
    if(!classOf[Turtle].isAssignableFrom(agents.`type`))
      throw new ExtensionException(
        "Expected input to be a turtleset")
  }
  def requireLinkBreed(context: Context, agents: AgentSet) {
    val world = context.getAgent.world
    if ((agents ne world.links) &&
        !world.asInstanceOf[org.nlogo.agent.World].isLinkBreed(agents.asInstanceOf[org.nlogo.agent.AgentSet]))
      throw new ExtensionException(
        I18N.errors.get("org.nlogo.prim.etc.$common.expectedLastInputToBeLinkBreed"))
  }
  def requireAlive(agent: Agent) {
    if (agent.id == -1)
      throw new ExtensionException(
        I18N.errors.get("org.nlogo.$common.thatAgentIsDead"))
  }
}

/// primitives

object ExtendedLinkNeighbors extends DefaultReporter with Helpers {
  override def getSyntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.LinksetType),
      Syntax.TurtlesetType,
      agentClassString = "-T--")
  override def report(args: Array[Argument], context: Context) = {
    val radius = args(0).getDoubleValue
    val linkBreed = args(1).getAgentSet
    if (radius < 0)
      throw new ExtensionException("radius cannot be negative")
    requireLinkBreed(context, linkBreed)
    Metrics.extendedLinkNeighbors(
      context.getAgent.asInstanceOf[org.nlogo.agent.Turtle],
      radius, linkBreed.asInstanceOf[org.nlogo.agent.AgentSet])
  }
}

object LinkDistance extends DefaultReporter with Helpers {
  override def getSyntax =
    Syntax.reporterSyntax(
      Array(Syntax.TurtleType, Syntax.LinksetType),
      Syntax.NumberType | Syntax.BooleanType,
      agentClassString = "-T--")
  override def report(args: Array[Argument], context: Context): AnyRef = {
    val destNode = args(0).getTurtle
    val linkBreed = args(1).getAgentSet
    requireLinkBreed(context, linkBreed)
    requireAlive(destNode)
    val result =
      Metrics.linkDistance(
        context.getAgent.asInstanceOf[org.nlogo.agent.Turtle],
        destNode.asInstanceOf[org.nlogo.agent.Turtle],
        linkBreed.asInstanceOf[org.nlogo.agent.AgentSet])
    result.map(Double.box(_))
      .getOrElse(java.lang.Boolean.FALSE)
  }
}

object MeanPathLength extends DefaultReporter with Helpers {
  override def getSyntax =
    Syntax.reporterSyntax(
      Array(Syntax.TurtlesetType, Syntax.LinksetType),
      Syntax.NumberType | Syntax.BooleanType)
  override def report(args: Array[Argument], context: Context): AnyRef = {
    val nodeSet = args(0).getAgentSet
    val linkBreed = args(1).getAgentSet
    requireTurtleset(nodeSet)
    requireLinkBreed(context, linkBreed)
    val result =
      Metrics.meanPathLength(
        nodeSet.asInstanceOf[org.nlogo.agent.AgentSet],
        linkBreed.asInstanceOf[org.nlogo.agent.AgentSet])
    result.map(Double.box(_))
      .getOrElse(java.lang.Boolean.FALSE)
  }
}

object PathTurtles extends DefaultReporter with Helpers {
  override def getSyntax =
    Syntax.reporterSyntax(
      Array(Syntax.TurtleType, Syntax.LinksetType),
      Syntax.ListType,
      agentClassString = "-T--")
  override def report(args: Array[Argument], context: Context) = {
    val destNode = args(0).getTurtle
    val linkBreed = args(1).getAgentSet
    requireLinkBreed(context, linkBreed)
    requireAlive(destNode)
    Metrics.pathTurtles(
      context.getRNG, context.getAgent.asInstanceOf[org.nlogo.agent.Turtle],
      destNode.asInstanceOf[org.nlogo.agent.Turtle],
      linkBreed.asInstanceOf[org.nlogo.agent.AgentSet])
  }
}

object PathLinks extends DefaultReporter with Helpers {
  override def getSyntax =
    Syntax.reporterSyntax(
      Array(Syntax.TurtleType, Syntax.LinksetType),
      Syntax.ListType,
      agentClassString = "-T--")
  override def report(args: Array[Argument], context: Context) = {
    val destNode = args(0).getTurtle
    val linkBreed = args(1).getAgentSet
    requireLinkBreed(context, linkBreed)
    requireAlive(destNode)
    Metrics.pathLinks(
      context.getRNG, context.getAgent.asInstanceOf[org.nlogo.agent.Turtle],
      destNode.asInstanceOf[org.nlogo.agent.Turtle],
      linkBreed.asInstanceOf[org.nlogo.agent.AgentSet])
  }
}
