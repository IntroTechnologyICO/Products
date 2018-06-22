/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.its360.parser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.its360.parser.entity.*;
import ru.its360.parser.extensions.rosreestr.ExtensionRosreestrManager;
import ru.its360.parser.extensions.rosreestr.entity.*;
import ru.utils.Property;

import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.its360.core.util.CoreUtils.ExceptionToString;

//import javax.enterprise.context.SessionScoped;

/**
 * @author Aleksandr Boytsov
 */
@Named
//@SessionScoped
public class ParserManager implements Serializable {

  private String sourceText;
  private String unidentifiedText;
  private String xmlMarkedText;
  private String xmlModelText;
  private String resultText;
  private String title;
  private String errors = "";

  private Parser parserModel;
  private Parser parserResult;
  private Markers markers;

  //  private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ParserManager.class.getName());
  private final Logger logger = LoggerFactory.getLogger(ParserManager.class);

  // keywords
  private final String exceptAll = "exceptAll";

  public ParserManager() {
  }

  public String getTable() {
    String res = null;

    for (ParcelDoc parcelDoc : parserResult.getData().getParcelDocs()) {
      res = res == null ? parcelDoc.toString() : res.concat(parcelDoc.toString());
    }

    return res;
  }

  public String getSimpleTableDoc(ParcelDoc parcelDoc) {
    StringBuilder print = new StringBuilder();

    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    print//.append("Титл;")
      .append("Участок;")
      .append("Номер регистрации;")
      .append("Дата регистрации;")
      .append("Тип записи;Описание регистрации;")
      .append("Клиент;Тип клиента;ИНН;Уступает;").append("Клиент;Тип клиента;ИНН;Уступает;").append("Клиент;Тип клиента;ИНН;Уступает;").append("Клиент;Тип клиента;ИНН;Уступает;")//.append("Клиент;Телефон;Тип клиента;ИНН;Уступает;").append("Клиент;Телефон;Тип клиента;ИНН;Уступает;").append("Клиент;Телефон;Тип клиента;ИНН;Уступает;").append("Клиент;Телефон;Тип клиента;ИНН;Уступает;").append("Клиент;Телефон;Тип клиента;ИНН;Уступает;").append("Клиент;Телефон;Тип клиента;ИНН;Уступает;")
      .append("Местоположение;Литер;")
      .append("Номер секции/подъезда;")
      .append("Этаж;")
      .append("Тип объекта;")
      .append("Количество комнат;")
      .append("Номер объекта;")
      .append("Общая площадь;").append("Жилая площадь;")//.append("Расчетная площадь;").append("Цена;")
      .append("Банк;").append("ИНН банка;").append("Тип организации банка;").append("Ставка;").append("Период;").append("Тип даты;")
//      .append("Дата страхования;").append("Номер страхования;").append("Страховщик;").append("Тип организации застройщика;")
//      .append("Дата передачи в собственность;")
//      .append("Тип;").append("Дата;").append("Сумма;").append("Тип;").append("Дата;").append("Сумма;").append("Тип;").append("Дата;").append("Сумма;").append("Тип;").append("Дата;").append("Сумма;").append("Тип;").append("Дата;").append("Сумма;")
//      .append("Исходник")
      .append("\n");

    for (Registration reg : parcelDoc.getRegistrations()) {
      for (Contract contract : reg.getContracts()) {

        for (RealtyObject object : contract.getObjects()) {

          String regDate = reg.getDate() != null ? df.format(reg.getDate()) : "";
          print
//            .append(title)
//            .append(";")
            .append(parcelDoc.getCn()).append(";")
            .append(reg.getNumber()).append(";")
            .append(regDate).append(";")
            .append(contract.getType()).append(";")
            .append(contract.getSource()).append(";");

          for (int i = 0; i < 4; i++) {
            if (contract.getClientDocs().size() > i) {
              String str = "";
              if (null != contract.getClientDocs().get(i).getName())
                str = contract.getClientDocs().get(i).getName().replace("\"", "");
              print.append(str).append(";")
//                .append(contract.getClientDocs().get(i).getPhone()).append(";")
                .append(contract.getClientDocs().get(i).getType()).append(";")
                .append(contract.getClientDocs().get(i).getInn()).append(";")
                .append(contract.getClientDocs().get(i).getConcede()).append(";");

            } else {
              print.append(";;;;");
            }
          }

          print.append(object.getBuildingDoc().getLocation()).append(";")
            .append(object.getBuildingDoc().getLiter()).append(";");

          print.append(object.getBuildingDoc().getSection().getName()).append(";");
          if (object.getBuildingDoc().getSection().getFloor() != null) {
            print.append(object.getBuildingDoc().getSection().getFloor().getNumber()).append(";");
            if (object.getBuildingDoc().getSection().getFloor().getFlatDoc() != null) {
              print.append(object.getBuildingDoc().getSection().getFloor().getFlatDoc().getFlatClass()).append(";");
              print.append(object.getBuildingDoc().getSection().getFloor().getFlatDoc().getRooms()).append(";");
              print.append(object.getBuildingDoc().getSection().getFloor().getFlatDoc().getNumber()).append(";");
              print.append(object.getBuildingDoc().getSection().getFloor().getFlatDoc().getAreaTotal()).append(";");
              print.append(object.getBuildingDoc().getSection().getFloor().getFlatDoc().getAreaLiving()).append(";");
//              print.append(object.getBuildingDoc().getSection().getFloor().getFlatDoc().getAreaCalc()).append(";");
            } else {
              print.append(";;;;;");
            }
          } else {
            print.append(";;;;;;");
          }

//          if (contract.getPaymentTerms().getPrice() != null) {
//            print.append(contract.getPaymentTerms().getPrice()).append(";");
//          } else {
//            print.append(";");
//          }

          if (contract.getPaymentTerms().getCredit() != null) {
            if (contract.getPaymentTerms().getCredit().getBankDoc() != null) {
              print.append(contract.getPaymentTerms().getCredit().getBankDoc().getName())
                .append(";")
                .append(contract.getPaymentTerms().getCredit().getBankDoc().getInn())
                .append(";")
                .append(contract.getPaymentTerms().getCredit().getBankDoc().getLegalForm())
                .append(";");
            } else {
              print.append(";;");
            }
            print.append(contract.getPaymentTerms().getCredit().getRate()).append(";")
              .append(contract.getPaymentTerms().getCredit().getPeriod()).append(";")
              .append(contract.getPaymentTerms().getCredit().getDateType()).append(";");
          } else {
            print.append(";;;");
          }

//          if (contract.getInshurance() != null) {
//            String inshuranceDate = contract.getInshurance().getDate() != null ? df.format(contract.getInshurance().getDate()) : "";
//
//            print.append(inshuranceDate).append(";")
//              .append(contract.getInshurance().getNumber()).append(";")
//              .append(contract.getInshurance().getInshurer().getName()).append(";")
//              .append(contract.getInshurance().getInshurer().getLegalForm()).append(";");
//          } else {
//            print.append(";;;");
//          }

//          if (contract.getDeveloperAgrees() != null && contract.getDeveloperAgrees().getSubmitObjectAgrees() != null) {
//            String submitDate = contract.getDeveloperAgrees().getSubmitObjectAgrees().getDateTo() != null ? df.format(contract.getDeveloperAgrees().getSubmitObjectAgrees().getDateTo()) : "";
//            print.append(submitDate).append(";");
//          } else {
//            print.append(";");
//          }

//          if (contract.getPaymentTerms().getPayments() != null) {
//            for (int i = 0; i < 10; i++) {
//              if (contract.getPaymentTerms().getPayments().size() > i) {
//                String paymentDate = contract.getPaymentTerms().getPayments().get(i).getDate() != null ? df.format(contract.getPaymentTerms().getPayments().get(i).getDate()) : "";
//                print.append(contract.getPaymentTerms().getPayments().get(i).getType()).append(";")
//                  .append(paymentDate).append(";")
//                  .append(contract.getPaymentTerms().getPayments().get(i).getSum()).append(";");
//              } else {
//                print.append(";;;");
//              }
//            }
//          }
//          print.append(reg.getText());
          print.append("\n");
        }

      }
    }

    return print.toString();
  }


  public String readInputStream(InputStream inputStream) {
    String result = "";
    ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

    try {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        resultStream.write(buffer, 0, length);
      }

      result = resultStream.toString("UTF-8");
    } catch (IOException e) {
      logger.error(ExceptionToString(e), e);
      e.printStackTrace();
    }

    return result;
  }


  public String parse(InputStream source, InputStream model, String title) {
    String doc = readInputStream(source);
    String mod = readInputStream(model);

    return parse(doc, mod, title);
  }


  public String parse(String source, String model, String docTitle) {
    title = docTitle;
    sourceText = source;
    xmlModelText = model;

    initialize();
    correct();
    markup();
    serialize();
    transform();
    addSource();
    save();

    return resultText;
  }

  public String parseToCsv(InputStream source, InputStream model, String title) {
    String doc = readInputStream(source);
    String mod = readInputStream(model);

    return parseToCsv(doc, mod, title);
  }


  public String parseToCsv(String source, String model, String docTitle) {
    title = docTitle;
    sourceText = source;
    xmlModelText = model;

    initialize();
    correct();
    markup();
    serialize();
    transform();
    addSource();
    save();

    return getTable();
  }

  private void initialize() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Parser.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      parserModel = (Parser) unmarshaller.unmarshal(new StringReader(xmlModelText));
    } catch (JAXBException ex) {
      logger.error(ExceptionToString(ex), ex);
    }
  }

  private void correct() {
    sourceText = sourceText
      .replaceAll("[\n\r]", " ")
      .replaceAll("[ ]+", " ")
      .replaceAll("[\\+]", "")
      .replaceAll("\\(", "`")
      .replaceAll("\\)", "`")
      .replaceAll("\\\\", "/");

    parserModel.getCorrectors().getCorrectors().forEach(corrector ->
      sourceText = sourceText.replaceAll(corrector.getFind(), corrector.getValue()));
  }

  private void markup() {
    // Маркировка текста
    HashMap<String, String[]> replaces = new LinkedHashMap<>();

//    logger.log(Level.SEVERE, sourceText);

    int replaceId = 0;

    for (Marker marker : parserModel.getMarkers().getMarkers()) {
      String findAttr = marker.getFind();

//      if ("date".equals(marker.getName()) || "(?[IXV]+ [А-я]+ 20[0-9][0-9])г\\.".equals(findAttr)) {
//        logger.info("debug point");
//      }

      findAttr = findAttr.replaceFirst("[(][\\?]", "(?<value>").replaceFirst("^[\\?]", "?<value>");
      findAttr = findAttr.replaceFirst("[(]_", "(?<mark>");

      Matcher m = Pattern.compile("(" + findAttr + ")").matcher(sourceText);

      while (m.find()) {

//        if (marker.getFind().contains("_")) {
//          logger.log(Level.INFO, "find _");
//        } else if (marker.getFind().contains("(?")) {
//          logger.log(Level.INFO, "find ?");
//        }

        String value = null;
        try {
          value = m.group("value");
        } catch (Exception e) {
//                    logger.log
//                        (Level.SEVERE, "marker name: " + marker.getName() + "\n" +
//                            "findAttr : " + findAttr + "\n" +
//                            "m.group : " + m.group() + "\n" +
//                            ExceptionToString(e), e);
        }

        String name = marker.getName();

//                findAttr = findAttr.replaceFirst("[(][\\?]", "(?<value>").replaceFirst("^[\\?]", "?<value>");

        String key;

        StringBuilder groupBefore = new StringBuilder();
        StringBuilder groupAfter = new StringBuilder();
        String mark = "0";
        Integer groupIdx = -1;
//        logger.log(Level.SEVERE, "findAttr --> " + findAttr);

        if (marker.getFind().lastIndexOf("(_") >= 0) {
          value = m.group("mark");
          key = m.group(0);
          mark = "1";

          // get groups
          if (m.groupCount() > 2) {
            for (int i = 2; i <= m.groupCount(); i++) {
              if (value.equals(m.group(i))) {
                groupIdx = i;
              }

              if (0 > groupIdx)
                groupBefore.append(m.group(i));
              else if (i > groupIdx)
                groupAfter.append(m.group(i));

            }
          }

//          logger.log(Level.SEVERE, "group --> " + m.group());
        } else {
          key = m.group(0);
        }

        if (!replaces.containsKey(key)) {
          replaces.put(key, new String[]{name, value, "#Replace_" + replaceId++ + "#", groupBefore.toString(), groupAfter.toString(), mark});
//          logger.log(Level.SEVERE,
//              String.format("gen replace: (m.getFind %s --> findAttr %s) key '%s' --> name '%s', value '%s', replaceId %s\n",
//                  marker.getFind(), findAttr, key, name, value, replaceId));
        }
      }
    }

    xmlMarkedText = sourceText;

    Iterator<Entry<String, String[]>> it = replaces.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, String[]> entry = it.next();
      String key = entry.getKey();
      String replaceItem = entry.getValue()[2];

      String before = entry.getValue()[3];
      String after = entry.getValue()[4];
      String mark = entry.getValue()[5];

      if ("0".equals(mark))
        xmlMarkedText = xmlMarkedText.replace(key, replaceItem);
      else
        xmlMarkedText = xmlMarkedText.replace(key, String.format("%s %s %s", before, replaceItem, after));
    }


    it = replaces.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, String[]> entry = it.next();
      String key = entry.getKey();
      String name = entry.getValue()[0];
      String value = entry.getValue()[1];
      String replaceItem = entry.getValue()[2];
      String mark = entry.getValue()[5];

      String resultItem;
      if ("0".equals(mark))
        resultItem = " <marker name='" + name + "'" + (value == null ? "" : " value='" + value + "'") + ">" + key + "</marker> ";
      else
        resultItem = " <marker name='" + name + "'" + (value == null ? "" : " value='" + value + "'") + ">" + value + "</marker> ";

//            logger.log(Level.SEVERE,
//                    String.format("try replace: replaceItem '%s' --> resultItem '%s'\n", replaceItem, resultItem));

      xmlMarkedText = xmlMarkedText.replace(replaceItem, resultItem);
    }

    unidentifiedText = xmlMarkedText
      .replaceAll(" [`:.,-]", " ")
      .replaceAll("<!-- (.*?)-->", " ")
      .replaceAll("<marker name='(.*?)'>(.*?)</marker>", " ")
      .replaceAll("[  ]+", " ");

    errors = "";
    Matcher m = Pattern.compile("[А-яA-z]+").matcher(unidentifiedText);
    while (m.find()) {
      errors += "Отсутствует в словаре (" + m.group(0) + ")\n";
    }

    xmlMarkedText = "<markers>" + xmlMarkedText + "</markers>";

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Markers.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      markers = (Markers) unmarshaller.unmarshal(new StringReader(xmlMarkedText));
    } catch (JAXBException ex) {
      logger.error(ExceptionToString(ex), ex);
    }

    xmlMarkedText = xmlMarkedText
      .replaceAll("<markers", "<div class='text-danger'")
      .replaceAll("</markers>", "</div>")
      .replaceAll("<marker name='(comment)'", " <span class='text-muted'")
      .replaceAll("<marker name='(parcel-id)' value='", "<br/><span class='badge bg-info' title='$1: ")
      .replaceAll("<marker name='(registration-number)' value='", "<br/><span class='badge bg-info' title='$1: ")
      .replaceAll("<marker name='(registration-number|person-name|company-name)' value='", "<span class='label label-info' title='$1: ")
      .replaceAll("<marker name='(sum)' value='", " <span class='label label-warning' title='$1: ")
      .replaceAll("<marker name='(number|date|area|procent|document-number)' value='", " <span class='label label-success' title='$1: ")
      .replaceAll("<marker name='([A-z-_]+)' value='", "<span class='label label-primary' title='$1: ")
      .replaceAll("<marker name='([A-z-_]+)'", " <span class='text-success' title='$1'")
      .replaceAll("</marker>", "</span> ")
      .replaceAll("[  ]+", " ");
  }

  private boolean validateRequredBlock(Block block) {
    return
      (null != block.isRequred() && block.isRequred()) ||

        block.getDataItems()
          .stream()
          .filter(i -> null != i.getRequred())
          .anyMatch(DataItem::getRequred) ||

        block.getBlocks()
          .stream()
          .map(this::validateRequredBlock)
          .anyMatch(b -> b);
  }

  private boolean validateRequred(Block source, Block block) {
    boolean requredValidated = true;
    boolean requredBlockValidated = true;

//    if (validateRequredBlock(source) || validateRequredBlock(block))
//      return requredValidated;


    for (Block bl : source.getBlocks()) {
      if (null != bl.isRequred() && bl.isRequred()) {
        requredBlockValidated = false;

        for (Block newItem : block.getBlocks()) {
          if (bl.getName().equals(newItem.getName())) {
            requredBlockValidated = true;
          }
        }
      }
//      requredValidated = block.getBlocks().stream().map(Block::getName).anyMatch(n -> bl.getName().equals(n));
    }

    for (DataItem item : source.getDataItems()) {
      requredValidated = false;

      if (item.getRequred() == null || (item.getRequred() != null && !item.getRequred())) {
        requredValidated = true;
        continue;
      }

      for (DataItem newItem : block.getDataItems()) {
        if (item.getName().equals(newItem.getName())) {
          requredValidated = true;
          continue;
        }
      }

      for (Marker marker : block.getMarkers()) {
        String[] finds = item.getFind().split(",");
        for (String find : finds) {
          if (find.trim().equals(marker.getName())) {
            requredValidated = true;
          }
        }
      }

      if (!requredValidated) {
        break;
      }
    }

    return requredValidated;//. && requredBlockValidated;
  }

  private void serializeItems(Block source, Block result) {
    // Заполняем данные
    source.getDataItems().stream().map((item) -> {
      LinkedList<Marker> findedMarkers = new LinkedList<>();
      result.getMarkers().forEach((marker) -> {
        String[] finds = item.getFind().split(",");
        for (String find : finds) {
          if (find.trim().equals(marker.getName())) {
            //if (item.getImportant()) {
            DataItem newItem = new DataItem(item.getName(), marker.getDataValue());
            newItem.setImportant(item.getImportant());
            newItem.setRequred(item.getRequred());
            result.getDataItems().add(newItem);
            //}
            findedMarkers.add(marker);
          }
        }
      });
      return findedMarkers;
    }).forEach((findedMarkers) -> result.getMarkers().removeAll(findedMarkers));
  }

  private void serializeBlock(Block source, Block result) {
    Block newBlock = null;
    boolean except = false;

    boolean validationEnd = source.getEnd() == null;

    markerloop:
    for (Marker marker : result.getMarkers()) {
      // Ищем начало блока
      if (source.getStart() != null) {
        String[] starts = source.getStart().split(",");
        for (String start : starts) {
          if (marker.getName().equals(start.trim())) {
            if (newBlock != null) {
              if (source.isRepeated() && validateRequred(source, newBlock)) {
                break markerloop;
              }
            } else {
              newBlock = new Block(source.getName());
              newBlock.setInternal(source.isInternal());
              newBlock.setRequred(source.isRequred());
              newBlock.setRepeated(source.isRepeated());
              newBlock.setTransparent(source.isTransparent());
              newBlock.setExcept(source.getExcept());
            }
          }
        }
      }

      // Ищем следующий блок
      if (source.getNext() != null) {
        String[] nexts = source.getNext().split(",");
        for (String next : nexts) {
          if (marker.getName().equals(next.trim())) {
            if (newBlock != null) {
              validationEnd = true;
              break markerloop;
            }
          }
        }
      }

      if (source.isExceptAll() && source.getInternalDataItemMarkerNames().stream().noneMatch(n -> marker.getName().equals(n))) {
        continue;
      }

      if (null != source.getExcept()) {
        if (Arrays.stream(source.getExcept()
          .split(","))
          .map(String::trim)
          .anyMatch(e -> marker.getName().equals(e))) {
          continue;
        }
      }


      // ищем блок except, который можно пропустить
      if (newBlock != null) {
        if (!"comment".equals(marker.getName())) {
          newBlock.getMarkers().add(marker);
        }
      }

      // Ищем последний элемент блока
      if (source.getEnd() != null)

      {
        String[] ends = source.getEnd().split(",");
        for (String end : ends) {
          if (marker.getName().equals(end.trim())) {
            if (newBlock != null) {
              validationEnd = true;
              break markerloop;
            }
          }
        }
      }
    }

    if (newBlock != null && validationEnd) {
      LinkedList<Marker> findedMarkers = new LinkedList<>(newBlock.getMarkers());
      //newBlock.setDebug(findedMarkers);

      // Заполним структуру данных
      serializeItems(source, newBlock);

      newBlock.setValidated(validateRequred(source, newBlock));
      //newBlock.setValidated(true);

      if (newBlock.isValidated()) {
        result.getBlocks().add(newBlock);

        if (!newBlock.isTransparent()) {
          result.getMarkers().removeAll(findedMarkers);
        }

        if (source.isRepeated()) {
          serializeBlock(source, result);
        }

        serialize(source, newBlock);

        if (newBlock.isTransparent()) {
          findedMarkers.removeAll(newBlock.getMarkers());
          result.getMarkers().removeAll(findedMarkers);
          newBlock.getMarkers().clear();
        }

        if (newBlock.getMarkers().size() > 0) {
          newBlock.setValidated(false);
        }
      }
    }
  }

  private void serialize(Block source, Block result) {
    // Заполним структуру блоков
    source.getBlocks().forEach(block -> serializeBlock(block, result));
  }

  private void serialize() {
    parserResult = new Parser();
    parserResult.setModels(new Models());

    parserModel.getModels().getModels().stream().map((model) -> {
      Model resultModel = new Model(model.getName());
      resultModel.setBlock(new Block(model.getBlock().getName()));
      resultModel.getBlock().setMarkers(new LinkedList<>());

      markers.getMarkers().stream()
        .filter((marker) -> (!"comment".equals(marker.getName())))
        .forEach((marker) -> resultModel.getBlock().getMarkers().add(marker));

      serialize(model.getBlock(), resultModel.getBlock());

      return resultModel;
    }).forEach((resultModel) -> parserResult.getModels().getModels().add(resultModel));

  }

  private void transform() {
    try {
      IDataModelTransformer dataModelParcer = (IDataModelTransformer) Class.forName(parserModel.getData().getParserClass()).newInstance();
      dataModelParcer.transform(parserResult, parserModel.getData());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      logger.error(ExceptionToString(ex), ex);
    }
  }

  private void addSource() {
    for (ParcelDoc parcelDoc : parserResult.getData().getParcelDocs()) {
      for (Registration reg : parcelDoc.getRegistrations()) {
        String findAttr = reg.getNumber();
        Matcher m = Pattern.compile("(" + reg.getNumber() + ")(.*?)([0-9][0-9][0-9-]/[0-9-][0-9-][0-9]/[0-9-][0-9-][0-9-][0-9-]-[0-9-]+)").matcher(sourceText);

        while (m.find()) {
          String value = null;
          try {
            value = m.group(2);
          } catch (Exception e) {
          }
          reg.setText(value);
        }
      }
    }
  }

  private void save() {
    JAXBContext jaxbContext;

    parserResult.setCorrectors(null);
    parserResult.setMarkers(null);

    Boolean findErrors = false;

    logger.info("parserResult is null? " + (null == parserResult));

    try {
      jaxbContext = JAXBContext.newInstance(Parser.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      java.io.StringWriter sw = new StringWriter();
      jaxbMarshaller.marshal(parserResult, sw);

      resultText = sw.toString();

      Matcher m = Pattern.compile("<marker (.*?)>(.*?)</marker>").matcher(resultText);
      while (m.find()) {
        findErrors = true;
        errors += "Не найден блок для маркера (" + m.group(1) + ") - " + m.group(2) + "\n";
      }

      unidentifiedText = errors;

    } catch (JAXBException ex) {
      logger.error(ExceptionToString(ex), ex);
    }

/*    if (!findErrors) {
      saveToDB(parserResult);
    } else {
      logger.log(Level.INFO, "not saved, errors " + errors);
    } */

    String res = getTable();
    System.out.println(res);

    //saveToDB(parserResult);
//
    try {
      //Files.write(Paths.get("/home/denis/prj/monitoring/tomcat_report.csv"), res.getBytes(), StandardOpenOption.APPEND);
      Files.write(Paths.get(Property.getProperty("upload_path") + "/tomcat_report.csv"), res.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      //exception handling left as an exercise for the reader
    }
  }

  private void saveToDB(Parser pr) {
    logger.info("save parcels");
    Data data = pr.getData();
    ExtensionRosreestrManager manager = new ExtensionRosreestrManager();
    manager.save(data);
  }

  public Data getData(String result) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Parser.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      parserModel = (Parser) unmarshaller.unmarshal(new StringReader(result));

      if (parserModel != null) {
        return parserModel.getData();
      }
    } catch (JAXBException ex) {
      logger.error(ExceptionToString(ex), ex);
    }
    return null;
  }

  public String getSourceText() {
    return sourceText;
  }

  public void setSourceText(String sourceText) {
    this.sourceText = sourceText;
  }

  public String getUnidentifiedText() {
    return unidentifiedText;
  }

  public void setUnidentifiedText(String unidentifiedText) {
    this.unidentifiedText = unidentifiedText;
  }

  public String getXmlMarkedText() {
    return xmlMarkedText;
  }

  public void setXmlMarkedText(String xmlMarkedText) {
    this.xmlMarkedText = xmlMarkedText;
  }

  public String getXmlModelText() {
    return xmlModelText;
  }

  public void setXmlModelText(String xmlModelText) {
    this.xmlModelText = xmlModelText;
  }

  public String getResultText() {
    return resultText;
  }

  public void setResultText(String resultText) {
    this.resultText = resultText;
  }

  public String getErrors() {
    return errors;
  }

  public void setErrors(String errors) {
    this.errors = errors;
  }

}
