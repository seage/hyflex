package hfu.parsers.cfg.pep;

import java.util.EventListener;

public interface ParserListener extends EventListener {
  void optionSet(ParserOptionEvent paramParserOptionEvent);
  
  void parserSeeded(EdgeEvent paramEdgeEvent);
  
  void edgePredicted(EdgeEvent paramEdgeEvent);
  
  void edgeScanned(EdgeEvent paramEdgeEvent);
  
  void edgeCompleted(EdgeEvent paramEdgeEvent);
  
  void parseComplete(ParseEvent paramParseEvent);
  
  void parseMessage(ParseEvent paramParseEvent, String paramString);
  
  void parseError(ParseErrorEvent paramParseErrorEvent) throws PepException;
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\ParserListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */