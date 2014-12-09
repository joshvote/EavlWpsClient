/**
 *
 */
package org.auscope.eavl.wpsclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fri096
 *
 */
public class WpsUtils {
	final static Logger logger = LoggerFactory.getLogger(WpsUtils.class);

	public static double[] getVectorResult(ExecuteResponseAnalyser analyser,
			String outputParameterName) throws WPSClientException,
			FileNotFoundException {
		String resultStr = WpsUtils.getResultString(analyser, outputParameterName);
		return parseWpsVectorOutput(resultStr);
	}


	public static double[] parseWpsVectorOutput(String resultStr) {
		double[] result = null;
		resultStr = resultStr.replaceAll("[\n\r]+", ",");
		String[] vals = resultStr.split(",");
		result = new double[vals.length];
		int c = 0;
		for (String val : vals) {
			if (val.equals("NA")) {
				result[c] = Double.NaN;
			} else {
				result[c] = Double.parseDouble(val.trim());
			}
			c++;
		}
		return result;
	}

	/**
	 * @param analyser
	 * @param outputParameterName
	 * @return
	 * @throws WPSClientException
	 * @throws FileNotFoundException
	 */
	public static String getResultString(ExecuteResponseAnalyser analyser,
			String outputParameterName) throws WPSClientException,
			FileNotFoundException {
		GenericFileDataBinding data = (GenericFileDataBinding) analyser
				.getComplexData(outputParameterName,
						GenericFileDataBinding.class);
		GenericFileData fileData = (GenericFileData) data.getPayload();
		File f = fileData.getBaseFile(false);

		XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		try {
			XPathExpression expression = xpath.compile("/xml-fragment");
			return expression.evaluate(new org.xml.sax.InputSource(
					new FileInputStream(f)));
		} catch (XPathExpressionException e) {
			throw new WPSClientException("Error parsing result: "
					+ e.getMessage(), e);
		} finally {
			if (!f.delete()) {
				logger.warn("Could not delete temporary result file: " + f);
			}
		}
	}
	public static double[][] getMatrixResult(ExecuteResponseAnalyser analyser,
			String outputParameterName) throws WPSClientException,
			FileNotFoundException {
		String resultStr = WpsUtils.getResultString(analyser, outputParameterName);
		return parseWpsMatrixOutput(resultStr);
	}

	/**
	 * @param resultStr
	 * @return
	 */
	public static double[][] parseWpsMatrixOutput(String resultStr) {
		double[][] result = null;

		String[] rows = resultStr.split("[\n\r]+");
		result = new double[rows.length][];
		int r = 0;
		for (String row : rows) {
			String[] vals = row.split(",");
			result[r] = new double[vals.length];
			int c = 0;
			for (String val : vals) {
				if (val.equals("NA")) {
					result[r][c] = Double.NaN;
				} else {
					result[r][c] = Double.parseDouble(val.trim());
				}
				c++;
			}
			c = 0;
			r++;
		}

		return result;
	}

}