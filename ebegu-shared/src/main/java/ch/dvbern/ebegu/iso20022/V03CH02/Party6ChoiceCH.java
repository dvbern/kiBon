
package ch.dvbern.ebegu.iso20022.V03CH02;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Party6Choice-CH complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Party6Choice-CH">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="OrgId" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}OrganisationIdentification4-CH"/>
 *           &lt;element name="PrvtId" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}PersonIdentification5-CH"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Party6Choice-CH", namespace = "http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd", propOrder = {
    "orgId",
    "prvtId"
})
public class Party6ChoiceCH {

    @XmlElement(name = "OrgId", namespace = "http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd")
    protected OrganisationIdentification4CH orgId;
    @XmlElement(name = "PrvtId", namespace = "http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd")
    protected PersonIdentification5CH prvtId;

    /**
     * Gets the value of the orgId property.
     * 
     * @return
     *     possible object is
     *     {@link OrganisationIdentification4CH }
     *     
     */
    public OrganisationIdentification4CH getOrgId() {
        return orgId;
    }

    /**
     * Sets the value of the orgId property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganisationIdentification4CH }
     *     
     */
    public void setOrgId(OrganisationIdentification4CH value) {
        this.orgId = value;
    }

    /**
     * Gets the value of the prvtId property.
     * 
     * @return
     *     possible object is
     *     {@link PersonIdentification5CH }
     *     
     */
    public PersonIdentification5CH getPrvtId() {
        return prvtId;
    }

    /**
     * Sets the value of the prvtId property.
     * 
     * @param value
     *     allowed object is
     *     {@link PersonIdentification5CH }
     *     
     */
    public void setPrvtId(PersonIdentification5CH value) {
        this.prvtId = value;
    }

}