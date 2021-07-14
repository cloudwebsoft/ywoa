package com.redmoon.webservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.redmoon.webservice package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _GetDirectoryOptions_QNAME = new QName(
			"http://webservice.redmoon.com/", "getDirectoryOptions");
	private final static QName _CreateFileResponse_QNAME = new QName(
			"http://webservice.redmoon.com/", "createFileResponse");
	private final static QName _CreateResponse_QNAME = new QName(
			"http://webservice.redmoon.com/", "createResponse");
	private final static QName _GetDirectoryOptionsResponse_QNAME = new QName(
			"http://webservice.redmoon.com/", "getDirectoryOptionsResponse");
	private final static QName _Create_QNAME = new QName(
			"http://webservice.redmoon.com/", "create");
	private final static QName _CreateFile_QNAME = new QName(
			"http://webservice.redmoon.com/", "createFile");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.redmoon.webservice
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link CreateFile }
	 * 
	 */
	public CreateFile createCreateFile() {
		return new CreateFile();
	}

	/**
	 * Create an instance of {@link GetDirectoryOptions }
	 * 
	 */
	public GetDirectoryOptions createGetDirectoryOptions() {
		return new GetDirectoryOptions();
	}

	/**
	 * Create an instance of {@link Create }
	 * 
	 */
	public Create createCreate() {
		return new Create();
	}

	/**
	 * Create an instance of {@link CreateResponse }
	 * 
	 */
	public CreateResponse createCreateResponse() {
		return new CreateResponse();
	}

	/**
	 * Create an instance of {@link CreateFileResponse }
	 * 
	 */
	public CreateFileResponse createCreateFileResponse() {
		return new CreateFileResponse();
	}

	/**
	 * Create an instance of {@link GetDirectoryOptionsResponse }
	 * 
	 */
	public GetDirectoryOptionsResponse createGetDirectoryOptionsResponse() {
		return new GetDirectoryOptionsResponse();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link GetDirectoryOptions }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservice.redmoon.com/", name = "getDirectoryOptions")
	public JAXBElement<GetDirectoryOptions> createGetDirectoryOptions(
			GetDirectoryOptions value) {
		return new JAXBElement<GetDirectoryOptions>(_GetDirectoryOptions_QNAME,
				GetDirectoryOptions.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link CreateFileResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservice.redmoon.com/", name = "createFileResponse")
	public JAXBElement<CreateFileResponse> createCreateFileResponse(
			CreateFileResponse value) {
		return new JAXBElement<CreateFileResponse>(_CreateFileResponse_QNAME,
				CreateFileResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link CreateResponse }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservice.redmoon.com/", name = "createResponse")
	public JAXBElement<CreateResponse> createCreateResponse(CreateResponse value) {
		return new JAXBElement<CreateResponse>(_CreateResponse_QNAME,
				CreateResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link GetDirectoryOptionsResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservice.redmoon.com/", name = "getDirectoryOptionsResponse")
	public JAXBElement<GetDirectoryOptionsResponse> createGetDirectoryOptionsResponse(
			GetDirectoryOptionsResponse value) {
		return new JAXBElement<GetDirectoryOptionsResponse>(
				_GetDirectoryOptionsResponse_QNAME,
				GetDirectoryOptionsResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Create }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservice.redmoon.com/", name = "create")
	public JAXBElement<Create> createCreate(Create value) {
		return new JAXBElement<Create>(_Create_QNAME, Create.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link CreateFile }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webservice.redmoon.com/", name = "createFile")
	public JAXBElement<CreateFile> createCreateFile(CreateFile value) {
		return new JAXBElement<CreateFile>(_CreateFile_QNAME, CreateFile.class,
				null, value);
	}

}
