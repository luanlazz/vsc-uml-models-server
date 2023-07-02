package com.vsc.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.vsc.demo.dao.AttributeEntity;
import com.vsc.demo.dao.ClassEntity;
import com.vsc.demo.dao.DiagramEntity;
import com.vsc.demo.dao.OperationEntity;
import com.vsc.demo.dao.OperationParameterEntity;
import com.vsc.demo.repository.AttributeRepository;
import com.vsc.demo.repository.ClassRepository;
import com.vsc.demo.repository.DiagramRepository;
import com.vsc.demo.repository.OperationParameterRepository;
import com.vsc.demo.repository.OperationRepository;
import com.vsc.demo.uml.models._class.ClassAttribute;
import com.vsc.demo.uml.models._class.ClassOperation;
import com.vsc.demo.uml.models._class.ClassStructure;
import com.vsc.demo.uml.models._class.OperationParameter;
import com.vsc.demo.uml.models._class.UMLElement;
import com.vsc.demo.uml.models._class.UMLModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Component("VSCUMLModel")
public class VSCUMLModel {

	@Autowired
	private ClassRepository classRepository;
	@Autowired
	private DiagramRepository diagramRepository;
	@Autowired
	private AttributeRepository attributeRepository;
	@Autowired
	private OperationRepository operationRepository;
	@Autowired
	private OperationParameterRepository parameterRepository;

	// Save a new model, read all elements and persist
	public void versionControlUml(UMLModel newModel) {
		DiagramEntity model = this.loadModelById(newModel.getId());
		if (model == null) {
			model = this.saveNewModel(newModel);
		} else {
			model = this.saveChanges(model, newModel);
		}
	}

	public DiagramEntity saveNewModel(UMLModel model) {
		DiagramEntity diagram = new DiagramEntity(model.getId(), model.getName(), model.getType());

		for (ClassStructure _class : model.getClasses()) {
			diagram.addClass(classStrucutureToEntity(_class, diagram));
		}

		diagramRepository.save(diagram);
		System.out.println("new model save: " + diagram.getName());

		return diagram;
	}

	public DiagramEntity loadModelById(String id) {
		DiagramEntity diagramEntity = diagramRepository.findByUmlId(id);
		return diagramEntity;
	}

	public DiagramEntity saveChanges(DiagramEntity model, UMLModel newModel) {
		DiagramEntity diagramEntity = new DiagramEntity();
		this.addedClass(model, newModel);
		this.removedClass(model, newModel);
		return diagramEntity;
	}

	public void addedClass(DiagramEntity model, UMLModel newModel) {
		List<ClassEntity> addClasses = new ArrayList<ClassEntity>();
		List<AttributeEntity> addAttributes = new ArrayList<AttributeEntity>();
		List<OperationEntity> addOperations = new ArrayList<OperationEntity>();
		List<OperationParameterEntity> addParameters = new ArrayList<OperationParameterEntity>();

		for (ClassStructure _class : newModel.getClasses()) {
			ClassEntity classExists = findClassById(model.getClasses(), _class.getId());
			if (classExists == null) {
				addClasses.add(classStrucutureToEntity(_class, model));
				continue;
			}

			for (ClassAttribute attribute : _class.getAttributes()) {
				AttributeEntity attributeExists = findAttributeById(classExists, attribute.getId());
				if (attributeExists == null) {
					addAttributes.add(classAttributeToEntity(attribute, classExists));
				}
			}

			for (ClassOperation operation : _class.getOperations()) {
				OperationEntity operationExists = findOperationById(classExists, operation.getId());
				if (operationExists == null) {
					addOperations.add(classOperationToEntity(operation, classExists));
					continue;
				}

				for (OperationParameter parameter : operation.getParameters()) {
					OperationParameterEntity parameterExists = findParameterById(operationExists, parameter.getId());
					if (parameterExists == null) {
						addParameters.add(opParameterToEntity(parameter, operationExists));
					}
				}
			}
		}

		for (ClassEntity _class : addClasses) {
			classRepository.save(_class);
			System.out.println("new class: " + _class.getName());
		}

		for (AttributeEntity attribute : addAttributes) {
			attributeRepository.save(attribute);
			System.out.println("new attribute: " + attribute.getName());
		}

		for (OperationEntity operation : addOperations) {
			operationRepository.save(operation);
			System.out.println("new operation: " + operation.getName());
		}

		for (OperationParameterEntity parameter : addParameters) {
			parameterRepository.save(parameter);
			System.out.println("new parameter: " + parameter.getName());
		}
	}

	public void removedClass(DiagramEntity model, UMLModel newModel) {
		List<ClassEntity> removedClasses = new ArrayList<ClassEntity>();
		List<AttributeEntity> removedAttributes = new ArrayList<AttributeEntity>();
		List<OperationEntity> removedOperations = new ArrayList<OperationEntity>();
		List<OperationParameterEntity> removedParameters = new ArrayList<OperationParameterEntity>();

		ArrayList<UMLElement> classes = new ArrayList<UMLElement>(newModel.getClasses());
		for (ClassEntity _class : model.getClasses()) {
			ClassStructure classExists = (ClassStructure) findById(classes, _class.getIdUml());
			if (classExists == null) {
				removedClasses.add(_class);
				continue;
			}

			ArrayList<UMLElement> attributes = new ArrayList<UMLElement>(classExists.getAttributes());
			for (AttributeEntity attribute : _class.getAttributes()) {
				ClassAttribute attributeExists = (ClassAttribute) findById(attributes, attribute.getIdUml());
				if (attributeExists == null) {
					removedAttributes.add(attribute);
				}
			}

			ArrayList<UMLElement> operations = new ArrayList<UMLElement>(classExists.getOperations());
			for (OperationEntity operation : _class.getOperations()) {
				ClassOperation operationExists = (ClassOperation) findById(operations, operation.getIdUml());
				if (operationExists == null) {
					removedOperations.add(operation);
					continue;
				}

				ArrayList<UMLElement> parameters = new ArrayList<UMLElement>(operationExists.getParameters());
				for (OperationParameterEntity parameter : operation.getParameters()) {
					OperationParameter parameterExists = (OperationParameter) findById(parameters,
							parameter.getIdUml());
					if (parameterExists == null) {
						removedParameters.add(parameter);
					}
				}
			}
		}
		
		for (ClassEntity _class : removedClasses) {
//			DiagramEntity diagram = _class.getDiagramEntity();
//			boolean removed = diagram.removeClass(_class);
//			if (removed) {
//				diagramRepository.save(diagram);
			classRepository.delete(_class);
			System.out.println("removed class: " + _class.getName());
//			}
		}
		
		for (AttributeEntity attribute : removedAttributes) {
//			ClassEntity _class = attribute.getClassEntity();
//			boolean removed = _class.removeAttribute(attribute);
//			if (removed) {
//				classRepository.save(_class);
			attributeRepository.delete(attribute);
			System.out.println("removed attribute: " + attribute.getName());
//			}
		}
		
		for (OperationEntity operation : removedOperations) {
//			ClassEntity _class = operation.getClassEntity();
//			boolean removed = _class.removeOperation(operation);
//			if (removed) {
//				classRepository.save(_class);
			operationRepository.delete(operation);
			System.out.println("removed operation: " + operation.getName());
//			}
		}
		
		for (OperationParameterEntity parameter : removedParameters) {
//			OperationEntity operation = parameter.getOperationEntity();
//			boolean removed = operation.removeParameter(parameter);
//			if (removed) {
//				operationRepository.save(operation);
			parameterRepository.delete(parameter);
//				System.out.println("removed parameter: " + parameter.getName());
//			}
		}		
	}

	public void alterClass() {
		// class (name, extends)

	}

	public void alterAttribute() {
		// attribute (name, type, visibility)

	}

	public void alterOperation() {
		// operation (return, name, type?, visibility)

	}

	public void alterParameter() {
		// parameter (name, type, value default)

	}

	public static UMLElement findById(ArrayList<UMLElement> elements, String id) {
		UMLElement element = null;

		for (UMLElement el : elements) {
			if (el.getId().equals(id)) {
				element = el;
				break;
			}
		}

		return element;
	}

	public static ClassEntity findClassById(List<ClassEntity> classes, String id) {
		ClassEntity classFind = null;

		for (ClassEntity _class : classes) {
			if (_class.getIdUml().equals(id)) {
				classFind = _class;
				break;
			}
		}

		return classFind;
	}

	public static AttributeEntity findAttributeById(ClassEntity _class, String attributeId) {
		AttributeEntity attributeFind = null;

		for (AttributeEntity attribute : _class.getAttributes()) {
			if (attribute.getIdUml().equals(attributeId)) {
				attributeFind = attribute;
				break;
			}
		}

		return attributeFind;
	}

	public static OperationEntity findOperationById(ClassEntity _class, String operationId) {
		OperationEntity operationFind = null;

		for (OperationEntity operation : _class.getOperations()) {
			if (operation.getIdUml().equals(operationId)) {
				operationFind = operation;
				break;
			}
		}

		return operationFind;
	}

	public static OperationParameterEntity findParameterById(OperationEntity operation, String parameterId) {
		OperationParameterEntity parameterFind = null;

		for (OperationParameterEntity parameter : operation.getParameters()) {
			if (parameter.getIdUml().equals(parameterId)) {
				parameterFind = parameter;
				break;
			}
		}

		return parameterFind;
	}

	public static ClassEntity classStrucutureToEntity(ClassStructure _class, DiagramEntity diagram) {
		ClassEntity classEntity = new ClassEntity(_class.getId(), diagram, _class.getName());

		for (ClassAttribute attribute : _class.getAttributes()) {
			classEntity.addAttribute(classAttributeToEntity(attribute, classEntity));
		}

		for (ClassOperation operation : _class.getOperations()) {
			classEntity.addOperation(classOperationToEntity(operation, classEntity));
		}

		return classEntity;
	}

	public static AttributeEntity classAttributeToEntity(ClassAttribute attribute, ClassEntity classEntity) {
		return new AttributeEntity(attribute.getId(), classEntity, attribute.getName(), null,
				attribute.getVisibility());
	}

	public static OperationEntity classOperationToEntity(ClassOperation operation, ClassEntity classEntity) {
		OperationEntity _operation = new OperationEntity(operation.getId(), classEntity, null, operation.getName(),
				null, operation.getVisibility(), null);

		for (OperationParameter operationParameter : operation.getParameters()) {
			_operation.addParameter(opParameterToEntity(operationParameter, _operation));
		}

		return _operation;
	}

	public static OperationParameterEntity opParameterToEntity(OperationParameter operationParameter,
			OperationEntity operation) {
		return new OperationParameterEntity(operationParameter.getId(), operation, operationParameter.getName(), null,
				null);
	}
}
